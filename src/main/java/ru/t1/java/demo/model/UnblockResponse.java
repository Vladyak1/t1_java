package ru.t1.java.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnblockResponse {
    private Long id;
    private boolean unblocked;
    private String message;

    public UnblockResponse(Long id, boolean unblocked) {
        this.id = id;
        this.unblocked = unblocked;
        this.message = unblocked ? "Successfully unblocked" : "Unblock request denied";
    }
}
