package com.boa.api.response;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class SouscriptionResponse extends GenericResponse {

    private Map<String, Object> data = new HashMap<>();
}
