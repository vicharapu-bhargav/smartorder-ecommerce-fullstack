package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Cart;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.CartDTO;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.payload.ProductResponse;
import com.ecommerce.sb_ecom.repositories.CartRepository;
import com.ecommerce.sb_ecom.repositories.CategoryRepository;
import com.ecommerce.sb_ecom.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository  cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.images.path}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category  category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        //Check if Product Name Already Exists
        List<Product> products = productRepository.findByCategory(category);
        for(Product product : products){
            if(product.getProductName().equals(productDTO.getProductName())){
                throw new APIException("Product Name Already Exists!!!");
            }
        }

        Product product = modelMapper.map(productDTO,Product.class);

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = productDTO. getPrice()*(1-(productDTO.getDiscount()*0.01));
        product.setSpecialPrice(specialPrice);
        Product savedProduct =  productRepository.save(product);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Pageable pageable = getCustomPage(pageNumber,pageSize,sortBy,sortOrder);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse = setProductResponse(productPage);
        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductResponse getAllProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        Pageable pageable = getCustomPage(pageNumber,pageSize,sortBy,sortOrder);
        Page<Product> productPage = productRepository.findByCategory(category,pageable);

        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = setProductResponse(productPage);
        productResponse.setContent(productDTOS);
        return productResponse;

    }

    @Override
    public ProductResponse getAllProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Pageable pageable = getCustomPage(pageNumber,pageSize,sortBy,sortOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageable);
        List<Product> products =  productPage.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse = setProductResponse(productPage);
        productResponse.setContent(productDTOS);
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));

        Product product = modelMapper.map(productDTO,Product.class);

        productFromDB.setProductName(product.getProductName());
        productFromDB.setDescription(product.getDescription());
        productFromDB.setQuantity(product.getQuantity());
        productFromDB.setPrice(product.getPrice());
        productFromDB.setDiscount(product.getDiscount());
        double specialPrice = product.getPrice()*(1-(product.getDiscount()*0.01));
        productFromDB.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(productFromDB);

        //Updating Product price in corresponding Carts having the product
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOs = carts.stream().map(cart->{
                        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                        List<ProductDTO> productDTOS = cart.getCartItems().stream()
                                .map(p->modelMapper.map(p.getProduct(), ProductDTO.class))
                                .toList();
                        cartDTO.setProducts(productDTOS);
                        return cartDTO;
                    }).toList();

        cartDTOs.forEach(cartDTO->cartService.updateProductInCarts(cartDTO.getCartId(),productId));

        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {

        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));

        //deleting the product from all user carts
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart->cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.delete(productFromDB);

        return modelMapper.map(productFromDB,ProductDTO.class);
    }

    @Override
    public ProductDTO uploadProductImage(Long productId, MultipartFile image) {

        //get the product from db
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",productId));

        //upload image to the server and get file name of uploaded image

        String fileName = fileService.uploadImage(path,image);

        //Updating the new file name to the Product and save it to DB
        productFromDB.setImage(fileName);
        Product updatedProduct = productRepository.save(productFromDB);

        //return ProductDTO
        return modelMapper.map(updatedProduct,ProductDTO.class);
    }


    public Pageable getCustomPage(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder){
        Sort sort = sortBy.equals("asc")
                ? Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);

        return pageable;
    }

    public ProductResponse setProductResponse(Page<Product> productPage) {
        ProductResponse productResponse = new ProductResponse();

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalItems(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }
}
