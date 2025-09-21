package dev.mkopp.mysite.shared.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class State<T, V> {
    private T value;
    private V error;
    private StatusNotification status;
}
