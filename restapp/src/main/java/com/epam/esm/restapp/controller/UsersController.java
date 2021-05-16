package com.epam.esm.restapp.controller;

import com.epam.esm.service.OrderService;
import com.epam.esm.service.UserService;
import com.epam.esm.service.dto.OrderDTO;
import com.epam.esm.service.dto.PaginationDTO;
import com.epam.esm.service.dto.UserDTO;
import com.epam.esm.service.exception.ValidationException;
import com.epam.esm.service.search.OrderCriteriaBuilder;
import com.epam.esm.service.search.SearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/users")
public class UsersController {

    private static final String SEARCH_BY_USER_ID= "user_id:";

    private final UserService userService;
    private final OrderService orderService;
    private final EntityResponseBuilder responseBuilder;

    @Autowired
    public UsersController(UserService userService, OrderService orderService, EntityResponseBuilder responseBuilder) {
        this.userService = userService;
        this.orderService = orderService;
        this.responseBuilder = responseBuilder;
    }

    /**
     * Retrieves users from repository according to provided request parameters.
     *
     * @param page (optional) request parameter for page number
     * @param size (optional) request parameter for page size
     * @param searchParameters (optional) request parameter for searching
     * @param orderParameters (optional) request parameter for sorting, ascending or descending
     * @return PagedModel<EntityModel<UserDTO>> object of users for returned page from repository
     * @throws ValidationException if provided query is not valid or users according to provided query
     *                          are not present in repository
     */
    @GetMapping()
    public PagedModel<EntityModel<UserDTO>> findByQuery(@RequestParam(value = "page", required = false) Long page,
                                     @RequestParam(value = "size", required = false) Integer size,
                                     @RequestParam(value = "search", required = false) String searchParameters,
                                     @RequestParam(value = "order", required = false) String orderParameters) {
        SearchCriteriaBuilder searchCriteriaBuilder = new SearchCriteriaBuilder(searchParameters);
        OrderCriteriaBuilder orderCriteriaBuilder = new OrderCriteriaBuilder(orderParameters);
        PaginationDTO paginationDTO = new PaginationDTO(page, size);
        List<UserDTO> users = userService.findByQuery(searchCriteriaBuilder.build(), orderCriteriaBuilder.build(),
                paginationDTO);
        return  responseBuilder.getUserPagedModel(users, paginationDTO, searchParameters, orderParameters);
    }

    /**
     * Returns UserDTO object for user with provided id from repository.
     *
     * @param id id of user to find
     * @return EntityModel<UserDTO> object of user with provided id in repository
     * @throws ValidationException if user with provided id is not present in repository
     */
    @GetMapping("/{id}")
    public EntityModel<UserDTO> findOne(@PathVariable Long id) {
        UserDTO userDTO = userService.find(id);
        return responseBuilder.getUserEntityModel(userDTO);
    }

    /**
     * Returns OrderDTO objects for user with provided id from repository.
     *
     * @param id id of user to find orders
     * @return PagedModel<EntityModel<OrderDTO>> object of orderDTO for user with provided id
     * @throws ValidationException if user with provided id is not present in repository
     */
    @GetMapping("/{id}/orders")
    public PagedModel<EntityModel<OrderDTO>> getUserOrders(@RequestParam(value = "page", required = false) Long page,
                                                     @RequestParam(value = "size", required = false) Integer size,
                                                     @PathVariable Long id) {
        PaginationDTO paginationDTO = new PaginationDTO(page, size);
        List<OrderDTO> orders = orderService.findByUser(id, paginationDTO);
        String searchParameter = SEARCH_BY_USER_ID + id;
        return  responseBuilder.getOrderPagedModel(orders, paginationDTO, searchParameter, null);
    }
}
