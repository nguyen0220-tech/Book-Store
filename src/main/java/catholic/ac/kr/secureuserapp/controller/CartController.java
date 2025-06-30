package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.AddToCartRequest;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CartDTO;
import catholic.ac.kr.secureuserapp.model.dto.UpdateCartRequest;
import catholic.ac.kr.secureuserapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@RequestParam Long userId) {
        CartDTO cartDTO = cartService.getCartByUserId(userId).getData();
        return ResponseEntity.ok(ApiResponse.success("Success", cartDTO));
    }

    @PostMapping("items")
    public ResponseEntity<ApiResponse<Void>> addCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request.getUserId(),request.getBookId(),request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Added successfully"));
    }

    @PutMapping("items")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(@RequestBody UpdateCartRequest request) {
        cartService.updateQuantity(request.getUserId(),request.getBookId(),request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Updated successfully"));
    }

    @DeleteMapping("items")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@RequestParam Long userId,@RequestParam Long bookId) {
        cartService.removeFromCart(userId,bookId);
        return ResponseEntity.ok(ApiResponse.success("Remove from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }




}
