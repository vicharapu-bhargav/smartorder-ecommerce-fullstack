package com.ecommerce.sb_ecom.repositories;

import com.ecommerce.sb_ecom.model.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId=?1 AND ci.product.productId=?2")
    CartItem findCartItemByCartIdAndProductId(Long cartId, Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.product.productId = ?2")
    void deleteByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByProduct_ProductId(Long productId);
}
