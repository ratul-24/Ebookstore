package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.Order;
import com.example.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://api-gateway:9090/users"; // User Service URL

    // ✅ Check if User Exists in User Microservice
    public boolean isUserValid(Long userId) {
        String url = USER_SERVICE_URL + "/" + userId;
        try {
            ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound e) {
            return false; // User does not exist
        } catch (Exception e) {
            throw new RuntimeException("Error checking user validity: " + e.getMessage());
        }
    }

    // ✅ Create Order (Only Validate User, No Payment MS)
    public Order createOrder(Order order) {
        if (!isUserValid(order.getUserId())) {
            throw new RuntimeException("User does not exist!");
        }

        // Set default payment status
        order.setStatus("Pending");

        // Save Order
        return orderRepository.save(order);
    }

    // ✅ Get All Orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ✅ Get Order by ID
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // ✅ Update Order Status
    public Order updateOrderStatus(Long id, String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(status);
            return orderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // ✅ Delete Order
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
