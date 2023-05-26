package com.globalforge.prettyfix;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.globalforge.infix.FixData;
import com.globalforge.infix.FixDataTagNameComparator;
import com.globalforge.infix.FixDataTagNumComparator;
import com.globalforge.infix.FixDataTagPosComparator;
import com.globalforge.infix.FixMessageMgr;
import com.globalforge.infix.qfix.FixGroupMgr;
import com.globalforge.infix.qfix.FixRepeatingGroup;

/**
 * @author mstarkie
 */
@Controller
public class PrettyFixController {
	public PrettyFixController() {
	}

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
	 * 
	 * @param data Contains the raw fix entered by user in the text area
	 * @return the pretty-fied fix returned by the infix engine.
	 */
	@PostMapping({ "/pretty-print" })
	public ModelAndView getPrettyFix(PrettyFixData data) {
		String fixInput = data.getInputFIX();
		fixInput = replaceControlChars(fixInput);
		fixInput = getCustomTag8(fixInput, data.getCustomDictionary());
		FixMessageMgr msgMgr;
		String displayString = "Error";
		try {
			msgMgr = new FixMessageMgr(fixInput);
			String msgType = msgMgr.getMsgType();
			FixGroupMgr grpMgr = msgMgr.getGroupMgr(msgType);
			Comparator<FixData> fieldComparator = new FixDataTagPosComparator();
			int sortOption = data.getSortOption();
			switch (sortOption) {
			case 0:
				break;
			case 1:
				fieldComparator = new FixDataTagNumComparator();
				break;
			case 2:
				fieldComparator = new FixDataTagNameComparator();
				break;
			default:
				break;
			}
			List<FixData> listFdd = msgMgr.getFixData();
			displayString = "";
			Collections.sort(listFdd, fieldComparator);
			String tab = "";
			FixRepeatingGroup group = null;
			boolean inGroup = false;
			boolean isDelimiter = false;
			for (FixData fdd : listFdd) {
				String tagNum = fdd.getTagNum();
				if (inGroup == false) {
					group = grpMgr.getGroup(tagNum);
					if (group != null) {
						inGroup = true;
					}
				} else {
					if (group.containsMember(tagNum)) {
						inGroup = true;
						tab = "&nbsp&nbsp&nbsp&nbsp";
						if (tagNum.equals(group.getDelimiter())) {
							isDelimiter = true;
						} else {
							isDelimiter = false;
						}
					} else {
						inGroup = false;
						tab = "";
					}
				}
				if (isDelimiter) {
					displayString += tab;
					displayString += "------------\n";
				}
				displayString += tab;
				displayString += fdd.getTagNum();
				displayString += (fdd.getTagName().isEmpty() ? "" : (" (" + fdd.getTagName() + ")"));
				displayString += " = " + fdd.getTagVal();
				displayString += (fdd.getTagDef().isEmpty() ? "" : (" (" + fdd.getTagDef() + ") "));
				displayString += "\n";
			}
			displayString = displayString.replace(" ", "&nbsp");
			displayString = displayString.replace("\n", "<br/>");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ModelAndView mav = new ModelAndView("pretty-print"); // pretty-print.html
		data.setOutputFIX(displayString);
		mav.addObject("outputFIX", data);
		return mav;
	}

	private String getCustomTag8(String fixMessage, int customDictOption) {
		String returnMsg = fixMessage;
		switch (customDictOption) {
		case 1: 
			returnMsg = fixMessage.replaceFirst("8=FIX.+?\u0001", "8=FIX.4.2Fid\u0001");
			break;
		default:
			break;
		}
		return returnMsg;
	}

	public String replaceControlChars(String fixMessage) {
		String returnMsg = fixMessage;
		returnMsg = returnMsg.replaceFirst("^.+?8=FIX", "8=FIX");
		returnMsg = returnMsg.replaceAll("\\r\\n|\\r|\\n", "");
		returnMsg = returnMsg.replaceAll("\\^A", "\u0001");
		returnMsg = returnMsg.replaceAll("\\|", "\u0001");
		return returnMsg;
	}

	public static void main(String[] args) {
		String testMsg = "8=FIX.4.2^A9=0059^A35=0^A49=CANTORINTL^A56=FLEXASIA^A34=460^A52=20230525-04:06:44^A10=035^A";
		testMsg = testMsg.replaceAll("\\r\\n|\\r|\\n", "");
		testMsg = testMsg.replaceAll("\\^A", "\u0001");

		testMsg = testMsg.replaceFirst("8=FIX.+?\u0001", "8=FIX.4.4Fid\u0001");
		System.out.println(testMsg);
	}
}
