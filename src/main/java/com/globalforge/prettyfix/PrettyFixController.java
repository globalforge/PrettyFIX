package com.globalforge.prettyfix;

import java.util.Comparator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.globalforge.infix.FixMessageMgr;
import com.globalforge.infix.api.InfixFieldInfo;
import com.globalforge.infix.api.InfixFieldInfoNameComparator;
import com.globalforge.infix.api.InfixFieldInfoPosComparator;
import com.globalforge.infix.api.InfixFieldInfoValComparator;

/**
 * 
 * @author mstarkie
 *
 */

@Controller
public class PrettyFixController {

	/**
	 * This method is called when when the pretty-print.html page is first loaded.
	 * 
	 * @param data
	 * @return
	 */
	@GetMapping({ "/" })
	public ModelAndView loadHomePage(PrettyFixData data) {
		ModelAndView mav = new ModelAndView("pretty-print"); // thymeleaf template
		return mav;
	}

	/**
	 * This method is called when the user hits the submit button.
	 * @param data Contains the raw fix entered by user in the text area
	 * @return the prettified fix returned by the infix engine.
	 */
	@PostMapping({ "/pretty-print" })
	public ModelAndView getPrettyFix(PrettyFixData data) {
		String properFix = data.getInputFIX().replaceAll("\\^A", "\u0001");
		FixMessageMgr msgMgr;
		String displayString = "Error";
		try {
			msgMgr = new FixMessageMgr(properFix);
			Comparator<InfixFieldInfo> fieldComparator =  new InfixFieldInfoPosComparator();
			int sortOption = data.getSortOption();
			switch (sortOption) {
			   case 0:
			      fieldComparator = new InfixFieldInfoPosComparator();
			      break;
			   case 1:
			      fieldComparator = new InfixFieldInfoValComparator();
               break;
			   case 2:
            default:
               break;
			}
			displayString = msgMgr.getInfixMap().toDisplayString(fieldComparator);
			displayString = displayString.replace( "(", "&nbsp(" );
			displayString = displayString.replace( "=", "&nbsp=&nbsp" );
			displayString = displayString.replace( "\n", "<br/>" );
		} catch (Exception e) {
			e.printStackTrace();
		}

		ModelAndView mav = new ModelAndView("pretty-print"); // pretty-print.html
		data.setOutputFIX(displayString);
		mav.addObject("outputFIX", data);
		return mav;
	}
}
