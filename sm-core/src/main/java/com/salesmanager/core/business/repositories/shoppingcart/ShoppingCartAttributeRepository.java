package com.salesmanager.core.business.repositories.shoppingcart;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salesmanager.core.model.shoppingcart.ShoppingCartAttributeItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartAttributeRepository extends JpaRepository<ShoppingCartAttributeItem, Long> {

    @Modifying
    @Query("DELETE FROM ShoppingCartAttributeItem att WHERE att.shoppingCartItem.id = ?1")
    void deleteByCartItemId(Long cartItemId);
}
