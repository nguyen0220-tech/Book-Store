package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.CartMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CartDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.Cart;
import catholic.ac.kr.secureuserapp.model.entity.CartItem;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.BookRepository;
import catholic.ac.kr.secureuserapp.repository.CartItemRepository;
import catholic.ac.kr.secureuserapp.repository.CartRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    public ApiResponse<CartDTO> getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        CartDTO cartDTO = cartMapper.toCartDTO(cart);

        double total = cartDTO.getItems().stream()      //Lấy danh sách các CartItemDTO từ DTO (giỏ hàng)
                .mapToDouble(item -> item.getPrice() * item.getQuantity()) //Biến mỗi CartItemDTO thành một giá trị double
                .sum();
        cartDTO.setTotalPrice(total);

        return ApiResponse.success("Success", cartDTO);
    }

    //khoi tao cart neu user chua co
    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with userId: " + userId));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return cartRepository.save(cart);
    }

    public ApiResponse<String> addToCart(Long userId, Long bookId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must NOT be negative");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("No book found with bookId: " + bookId));

        // Tìm xem sách này đã có trong giỏ hàng chưa
        Optional<CartItem> optionalCartItem = cart.getItems().stream()
                .filter(ci -> ci.getBook().getId().equals(bookId))
                .findFirst();

        // Nếu đã có sách trong giỏ thì cộng thêm quantity
        if (optionalCartItem.isPresent()) {
            CartItem item = optionalCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Nếu chưa có thì tạo mới CartItem
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setBook(book);
            ci.setQuantity(quantity);
            cart.getItems().add(ci);
        }
        cartRepository.save(cart);
        return ApiResponse.success("Added " + quantity + " to the cart");
    }

    public void updateQuantity(Long userId, Long bookId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must NOT be negative");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with userId: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No book found with bookId: " + bookId));

        if (quantity == 0) {
            cart.getItems().remove(item); // Xóa khỏi giỏ
            cartItemRepository.delete(item);  // Xóa DB
        } else {
            item.setQuantity(quantity); // Cập nhật số lượng
            cartItemRepository.save(item); // Lưu thay đổi
        }
    }

    public void removeFromCart(Long userId, Long bookId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with userId: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No book found with bookId: " + bookId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with userId: " + userId));

        cartItemRepository.deleteAll(cart.getItems());

        cart.getItems().clear(); // Xóa khỏi bộ nhớ

        cartRepository.save(cart); // Lưu giỏ đã rỗng
    }
}
