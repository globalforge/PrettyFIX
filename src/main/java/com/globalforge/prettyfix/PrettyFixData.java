package com.globalforge.prettyfix;

import java.util.List;

import com.globalforge.infix.FixData;

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
	private int customDictionary;
	private List<FixData> fixData;
}
