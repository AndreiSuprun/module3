package com.epam.esm.dao;

import com.epam.esm.entity.Tag;

import java.util.List;

/**
 * DAO interface responsible for additional to CRUD handling operations for tag entities
 *
 * @author Andrei Suprun
 */
public interface TagDAO extends GenericDAO<Tag> {

//    /**
//     * Retrieves count of gift certificates from repository for tag with provided id.
//     *
//     * @param id id of tag for which is necessary to count gift certificates
//     * @return Integer count of gift certificates for tag with provided id
//     */
//

    List<Tag> findAll(Long page, Integer size);
}
