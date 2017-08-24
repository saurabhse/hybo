package com.hackovation.hybo.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TLHResponse {

List<TLHInternalResponse> iternalList = new ArrayList<>();
}
