package com.hackovation.hybo.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RebalanceResponse {

	List<EtfParent> name = new ArrayList<>();
	
	
}
