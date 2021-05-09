package com.epam.esm.service.impl;

import com.epam.esm.dao.OrderDAO;
import com.epam.esm.dao.criteria.OrderCriteria;
import com.epam.esm.dao.criteria.SearchCriteria;
import com.epam.esm.entity.GiftCertificate;
import com.epam.esm.entity.Order;
import com.epam.esm.entity.OrderItem;
import com.epam.esm.service.GiftCertificatesService;
import com.epam.esm.service.OrderService;
import com.epam.esm.service.UserService;
import com.epam.esm.service.dto.*;
import com.epam.esm.service.exception.ErrorCode;
import com.epam.esm.service.exception.ProjectException;
import com.epam.esm.service.mapper.impl.OrderItemMapper;
import com.epam.esm.service.mapper.impl.OrderMapper;
import com.epam.esm.service.validator.impl.OrderItemValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderDAO orderDAO;
    private UserService userService;
    private GiftCertificatesService certificatesService;
    private OrderItemValidator orderItemValidator;

    private OrderMapper mapper;
    private OrderItemMapper orderItemMapper;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO, UserService userService, GiftCertificatesService certificatesService,
                            OrderItemValidator orderItemValidator, OrderMapper mapper, OrderItemMapper orderItemMapper) {
        this.orderDAO = orderDAO;
        this.userService = userService;
        this.certificatesService = certificatesService;
        this.orderItemValidator = orderItemValidator;
        this.mapper = mapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public List<OrderDTO> findByQuery(List<SearchCriteria> searchParams, List<OrderCriteria> orderParams,
                                     PaginationDTO paginationDTO) {
        checkPagination(paginationDTO);
        Long count = orderDAO.count(searchParams);
        checkPageNumber(paginationDTO, count);
        List<Order> orders = orderDAO.findByQuery(searchParams, orderParams, paginationDTO.getPage(), paginationDTO.getSize());
        return orders.stream().map(mapper::mapEntityToDTO).collect(Collectors.toList());
    }

    @Override
    public OrderDTO find(Long id) {
        Order user = orderDAO.findOne(id);
        if (user == null) {
            throw new ProjectException(ErrorCode.ORDER_NOT_FOUND, id);
        }
        return mapper.mapEntityToDTO(user);
    }

    @Transactional
    @Override
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        UserDTO user = userService.find(orderDTO.getUser().getId());
        if (orderDTO.getCertificates().isEmpty()) {
            throw new ProjectException(ErrorCode.CERTIFICATES_NOT_ADDED);
        }
        BigDecimal totalPrice = BigDecimal.valueOf(orderDTO.getCertificates().stream().
                mapToInt(item -> item.getGiftCertificateDTO().getPrice().intValue() * item.getQuantity()).sum());
        orderDTO.setTotalPrice(totalPrice);
        orderDTO.setCreateDate(LocalDateTime.now());
        orderDTO.setUser(user);
        List<OrderItemDTO> orderItemDTOList = orderDTO.getCertificates();
        Order order = mapper.mapDtoToEntity(orderDTO);
        orderDAO.insert(order);
        for (OrderItemDTO orderItemDTO : orderItemDTOList){
            OrderItem orderItem = orderItemMapper.mapDtoToEntity(orderItemDTO);
            GiftCertificate certificate = orderItem.getCertificate();
            if(certificatesService.find(certificate.getId()) == null){
                throw new ProjectException(ErrorCode.CERTIFICATE_NOT_FOUND, certificate.getId());
            }
            orderItemValidator.validate(orderItem);
            orderItem.setOrder(order);
            order.addOrderCertificate(orderItem);
        }
        Order orderInDB = orderDAO.update(order, order.getId());
        return mapper.mapEntityToDTO(orderInDB);
    }

    @Transactional
    @Override
    public OrderDTO update(OrderDTO orderDto, Long id) {
        Order order = orderDAO.findOne(id);
        if (order == null) {
            throw new ProjectException(ErrorCode.ORDER_NOT_FOUND, id);
        }
        Order orderInRequest = mapper.mapDtoToEntity(orderDto);
       // validator.validate(orderInRequest);
        order = orderDAO.update(orderInRequest, id);
        return mapper.mapEntityToDTO(order);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        orderDAO.delete(id);
    }
}
