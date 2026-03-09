package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Cart;
import com.ecommerce.sb_ecom.model.CartItem;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.CartDTO;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.repositories.CartItemRepository;
import com.ecommerce.sb_ecom.repositories.CartRepository;
import com.ecommerce.sb_ecom.repositories.ProductRepository;
import com.ecommerce.sb_ecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart or create one
        Cart userCart = createCart();

        //Retrieve Product Details
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        //Perform Validations
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(userCart.getCartId(),productId);

        if(cartItem!=null){
            throw new APIException("Product already Exists in the Cart!!!");
        }

        if(product.getQuantity()==0){
            throw new APIException("Product "+ product.getProductName()+" is not available !!!");
        }

        if(product.getQuantity()<quantity){
            throw new APIException("Please Order the Product "+ product.getProductName()+" less than equal to Quantity: " +product.getQuantity()+".");
        }

        //Create Cart Item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(userCart);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());
        //productRepository.save(product);

        //Save Cart Item and update cart price
        userCart.setTotalPrice(userCart.getTotalPrice() + (product.getSpecialPrice()*quantity));
        userCart.getCartItems().add(newCartItem);
        cartRepository.save(userCart);

        // Return Updated Cart
        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);
        List<CartItem> cartItems = userCart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item->{
            //We are setting quantity to what user has ordered otherwise it will have the total stock quantity
            ProductDTO map = modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });


        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> cartList =cartRepository.findAll();

        if(cartList.isEmpty()){
            throw new APIException("No Cart Exists !!!");
        }

        List<CartDTO> cartDTOList = cartList.stream()
                                    .map(cart->{
                                        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                                        List<ProductDTO> productDTOList = cart.getCartItems().stream()
                                                .map(cartItem -> {
                                                     ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                                                     productDTO.setQuantity(cartItem.getQuantity());
                                                     return productDTO;})
                                                .toList();
                                        cartDTO.setProducts(productDTOList);
                                        return cartDTO;})
                                    .toList();
        return cartDTOList;
    }

    @Override
    public CartDTO getCartByIdAndEmail() {

        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        Long cartId = cart.getCartId();

        Cart userCart  = cartRepository.findCartByEmailAndCartId(email,cartId);

        if(userCart==null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }

        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);

        //Setting quantity of the product to cartItem quantity
        userCart.getCartItems().forEach(cartItem->{
            cartItem.getProduct().setQuantity(cartItem.getQuantity());
        });

        List<ProductDTO> productDTOList = userCart.getCartItems().stream()
                .map(cartItem -> modelMapper.map(cartItem.getProduct(),ProductDTO.class))
                .toList();
        cartDTO.setProducts(productDTOList);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        Long cartId = userCart.getCartId();

        if(userCart==null){
            throw new ResourceNotFoundException("Cart","cartId",userCart.getCartId());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        if(product.getQuantity()==0){
            throw new APIException("Product "+ product.getProductName()+" is not available !!!");
        }

        if(product.getQuantity()<quantity){
            throw new APIException("Please Order the Product "+ product.getProductName()+" less than equal to Quantity: " +product.getQuantity()+".");
        }


        CartItem cartItem =  cartItemRepository.findCartItemByCartIdAndProductId(userCart.getCartId(),productId);
        if(cartItem==null){
            throw new APIException("Product "+ product.getProductName()+" is not available in the Cart !!!");
        }

        int newQuantity = cartItem.getQuantity()+quantity;
        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if (newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        }
        else{
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            userCart.setTotalPrice(userCart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            //cartRepository.save(userCart);
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        if(savedCartItem.getQuantity() == 0){
            cartItemRepository.deleteById(savedCartItem.getCartItemId());
        }


        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);
        List<ProductDTO> productDTOList = userCart.getCartItems().stream()
                .map(cartItem1 -> {
                    ProductDTO productDTO = modelMapper.map(cartItem1.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(cartItem1.getQuantity());
                    return productDTO;
                })
                .toList();

        cartDTO.setProducts(productDTOList);
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart userCart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));


        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId,productId);
        if(cartItem==null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }

        userCart.setTotalPrice(userCart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByCartIdANDProductId(cartId, productId);

        return "Product "+cartItem.getProduct().getProductName()+" has been removed from the Cart...";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cartId,productId);
        if(cartItem==null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }

        double cartPrice = cart.getTotalPrice() -  (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        CartItem savedCartItem = cartItemRepository.save(cartItem);
    }


    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }
        Cart cart =  new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
