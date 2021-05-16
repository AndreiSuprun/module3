package com.epam.esm.dao.impl;

import com.epam.esm.dao.GiftCertificateDAO;
import com.epam.esm.dao.TagDAO;
import com.epam.esm.dao.criteria.CriteriaUtil;
import com.epam.esm.dao.criteria.OrderCriteria;
import com.epam.esm.dao.criteria.SearchCriteria;
import com.epam.esm.entity.GiftCertificate;
import com.epam.esm.entity.Order;
import com.epam.esm.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GiftCertificateDAOImpl implements GiftCertificateDAO {

    @PersistenceContext
    private EntityManager entityManager;
    private final CriteriaUtil<GiftCertificate> criteriaUtil;
    private final TagDAO tagDAO;

    @Autowired
    public GiftCertificateDAOImpl(CriteriaUtil<GiftCertificate> criteriaUtil, TagDAO tagDAO) {
        this.criteriaUtil = criteriaUtil;
        this.tagDAO = tagDAO;
    }

    @Override
    public GiftCertificate findOne(Long id) {
        return entityManager.find(GiftCertificate.class, id);
    }

    @Override
    public GiftCertificate findByName(String name) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GiftCertificate> query = builder.createQuery(GiftCertificate.class);
        Root<GiftCertificate> root = query.from(GiftCertificate.class);
        query.select(root).where(builder.equal(root.get("name"), name));
        TypedQuery<GiftCertificate> typedQuery = entityManager.createQuery(query);
        List<GiftCertificate> giftCertificates = typedQuery.getResultList();
        if (!giftCertificates.isEmpty()) {
            return giftCertificates.iterator().next();
        }
        return null;
    }

    public List<Order> getOrdersForCertificates(Long certificateId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> orderRoot = criteriaQuery.from(Order.class);
        criteriaQuery.where(criteriaBuilder.equal(orderRoot.join("orderItems").get("certificate").get("id"), certificateId));
        criteriaQuery.select(orderRoot);
        TypedQuery<Order> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public boolean delete(Long id) {
        GiftCertificate giftCertificate = entityManager.find(GiftCertificate.class, id);
        if (giftCertificate != null) {
            entityManager.remove(giftCertificate);
            return true;
        }
        return false;
    }

    @Override
    public Long count(List<SearchCriteria>... searchParams) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<GiftCertificate> root = query.from(GiftCertificate.class);
        if (searchParams.length > 0) {
            query.where(criteriaUtil.buildSearchCriteriaPredicate(searchParams[0], builder,
                    root));
        }
        query.select(builder.count(root));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public GiftCertificate insert(GiftCertificate certificate) {
        addTags(certificate);
        entityManager.persist(certificate);
        return certificate;
    }

    @Override
    public GiftCertificate update(GiftCertificate certificate, Long id) {
        certificate.setId(id);
        addTags(certificate);
        return entityManager.merge(certificate);
    }

    @Override
    public List<GiftCertificate> findByQuery(List<SearchCriteria> searchParams, List<OrderCriteria> sortParams, Long page, Integer size) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GiftCertificate> query = builder.createQuery(GiftCertificate.class);
        Root<GiftCertificate> root = query.from(GiftCertificate.class);
        query.select(root);
        query.where(criteriaUtil.buildSearchCriteriaPredicate(searchParams, builder, root));
        query.orderBy(criteriaUtil.addSortCriteria(sortParams, builder, root));
        TypedQuery<GiftCertificate> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) ((page - 1) * size));
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }

    private void addTags(GiftCertificate certificate) {
        List<Tag> tags = certificate.getTags();
        List<Tag> tagsInDB = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getId() != null) {
                Tag tagInDB = tagDAO.findOne(tag.getId());
                if (tagInDB != null) {
                    tagsInDB.add(tagInDB);
                } else {
                    tag.setId(null);
                    tagDAO.insert(tag);
                    tagsInDB.add(tagDAO.findByName(tag.getName()));
                }
            } else {
                tagDAO.insert(tag);
                tagsInDB.add(tagDAO.findByName(tag.getName()));
            }
        }
        certificate.setTags(tagsInDB);
    }
}
