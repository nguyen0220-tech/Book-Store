package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.Status.OrderStatus;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.OrderDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.OrderRequest;
import catholic.ac.kr.secureuserapp.model.entity.Order;
import catholic.ac.kr.secureuserapp.repository.OrderRepository;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.InvoiceService;
import catholic.ac.kr.secureuserapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.checkout(userDetails.getUser().getId(), request));
    }

    @GetMapping("my-order")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrder(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(orderService.getOrderByUserId(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("admin/all")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @GetMapping("admin/all-desc")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrdersDesc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(orderService.getAllOrdersAndFilter(page, size));
    }

    @GetMapping("admin/all-asc")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrdersAsc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(orderService.getAllOrdersAndFilterASC(page, size));
    }

    @GetMapping("admin/all-createdAt")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrdersCreatedAt(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(orderService.getAllOrdersAndFilterCreated(page, size));
    }

    @GetMapping("admin/all-status")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrdersByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.getAllOrdersAndFilterByStatus(page, size, status));
    }

    @PutMapping("admin/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @GetMapping("count-not-confirmed")
    public ResponseEntity<ApiResponse<Integer>> getCountOrdersNotConfirmed() {
        return ResponseEntity.ok(orderService.countOrdersNotConfirmed());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<String>> deleteOrderHistory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.deleteOrderById(id));
    }

    @PutMapping("{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(userDetails.getUser().getId(), id));
    }

    @GetMapping("{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId, Principal principal) { //Principal là interface đại diện cho chủ thể đã được xác thực
        Order order = orderRepository.getOrderByIdAndUser(orderId, principal.getName());

        try {
            byte[] pdfBytes = invoiceService.generateInvoicePdf(order);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline() //chỉ muốn xem file PDF trực tiếp trên trình duyệt
                    .filename("invoice-" + orderId + ".pdf")
                    .build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }
}
