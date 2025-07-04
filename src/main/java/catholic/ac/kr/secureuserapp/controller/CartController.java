package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.AddToCartRequest;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CartDTO;
import catholic.ac.kr.secureuserapp.model.dto.UpdateCartRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal MyUserDetails userDetails) {
        CartDTO cartDTO = cartService.getCartByUserId(userDetails.getUser().getId()).getData();
        return ResponseEntity.ok(ApiResponse.success("Success", cartDTO));
    }

    @PostMapping("items")
    public ResponseEntity<ApiResponse<Void>> addCart(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody AddToCartRequest request) {
        cartService.addToCart(userDetails.getUser().getId(), request.getBookId(),request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Added successfully"));
    }

    @PutMapping("items")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody UpdateCartRequest request) {
        cartService.updateQuantity(userDetails.getUser().getId(), request.getBookId(),request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Updated successfully"));
    }

    @DeleteMapping("items")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam Long bookId) {
        cartService.removeFromCart(userDetails.getUser().getId(), bookId);
        return ResponseEntity.ok(ApiResponse.success("Remove from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal MyUserDetails userDetails) {
        cartService.clearCart(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
