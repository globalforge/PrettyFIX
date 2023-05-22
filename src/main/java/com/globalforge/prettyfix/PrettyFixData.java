package com.globalforge.prettyfix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrettyFixData {
	private String inputFIX;
	private String outputFIX;
	private int sortOption;
}
