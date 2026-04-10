package tn.spring.gateway.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionProxyController {
    private final ProxyForwarder proxy;
    private static final String DISCUSSION_BASE = "http://localhost:8087/api/discussions"; // Port du nouveau service

    public DiscussionProxyController(ProxyForwarder proxy) { this.proxy = proxy; }


    @PostMapping("/groups")
    public ResponseEntity<String> createGroup(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(DISCUSSION_BASE + "/groups", HttpMethod.POST, body, req);
    }

    @GetMapping("/groups/user/{userId}")
    public ResponseEntity<String> getMyGroups(@PathVariable String userId, HttpServletRequest req) {
        return proxy.forward(DISCUSSION_BASE + "/groups/user/" + userId, HttpMethod.GET, null, req);
    }

    @GetMapping("/groups/all")
    public ResponseEntity<String> getAllGroups(HttpServletRequest req) {
        return proxy.forward(DISCUSSION_BASE + "/groups/all", HttpMethod.GET, null, req);
    }

    @PutMapping("/groups/{id}")
    public ResponseEntity<String> updateGroup(@PathVariable String id, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        return proxy.forward(DISCUSSION_BASE + "/groups/" + id, HttpMethod.PUT, body, req);
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable String id, HttpServletRequest req) {
        return proxy.forward(DISCUSSION_BASE + "/groups/" + id, HttpMethod.DELETE, null, req);
    }

    // Dans DiscussionProxyController.java (Projet GATEWAY - 8090)

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<String> getHistory(@PathVariable String groupId, HttpServletRequest req) {
        // On définit l'adresse du microservice qui contient les messages (port 8087)
        String backendUrl = "http://localhost:8087/api/discussions/groups/" + groupId + "/messages";

        System.out.println("Gateway forwarding to: " + backendUrl);

        // On transfère la requête
        return proxy.forward(backendUrl, HttpMethod.GET, null, req);
    }

}
