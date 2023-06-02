package com.globalforge.prettyfix;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
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
   public static String TAB = "&nbsp";

   public PrettyFixController() {
   }

   /**
    * This method is called when when the pretty-print.html page is first
    * loaded.
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
      Deque<String> curGroup = new ArrayDeque<>();
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
         data.setFixData(listFdd);
         for (FixData fdd : listFdd) {
            String tagNum = fdd.getTagNum();
            FixRepeatingGroup group = grpMgr.getGroup(tagNum);
            // start of repeating group.
            if (group != null) {
               // display as regular tag but push the group onto the stack so next fields (group members) will be indented.
               displayString += getDisplayText(fdd, curGroup.size(), sortOption);
               curGroup.push(tagNum);
               continue;
            }
            if (curGroup.isEmpty()) {
               // the case when we receive a regular field (not part of any group)
               displayString += getDisplayText(fdd, curGroup.size(), sortOption);
               continue;
            }
            // we are inside a repeating group here
            String curGoupTagNum = curGroup.peek();
            group = grpMgr.getGroup(curGoupTagNum);
            if (tagNum.equals(group.getDelimiter())) {
               // display a line separating the repeating tags.
               displayString += getDelimiterLine(curGroup.size(), sortOption);
            }
            if (group.containsMember(tagNum)) {
               // indent the line, the number of tabs is determined by how deep the nesting is.
               displayString += getDisplayText(fdd, curGroup.size(), sortOption);
               continue;
            } else {
               boolean found = false;
               // coming out of repeating group.
               while (!found && curGroup.size() > 0) {
                  curGoupTagNum = curGroup.peek();
                  group = grpMgr.getGroup(curGoupTagNum);
                  // nested groups and the current tags belongs to on outer group.
                  if (group != null && group.containsMember(tagNum)) {
                     found = true;
                  } else {
                     // not part of the current nested group so pop stack and check the outer group.
                     curGroup.pop();
                  }
               }
               if (tagNum.equals(group.getDelimiter())) {
                  // always display a line before displaying the first tag in the set of repeating tags.
                  displayString += getDelimiterLine(curGroup.size(), sortOption);
               }
               displayString += getDisplayText(fdd, curGroup.size(), sortOption);
            }
         }
         displayString = toHTML(displayString);
      } catch (Exception e) {
         e.printStackTrace();
      }
      ModelAndView mav = new ModelAndView("pretty-print"); // pretty-print.html
      data.setOutputFIX(displayString);
      mav.addObject("outputFIX", data);
      return mav;
   }

   private String toHTML(String displayString) {
      displayString = displayString.replace(" ", "&nbsp");
      displayString = displayString.replace("\n", "<br/>");
      return displayString;
   }

   private String getDisplayText(FixData fdd, int nestingLevel, int sortOption) {
      String displayString = "";
      nestingLevel = sortOption > 0 ? 0 : nestingLevel;
      for (int i = 0; i < nestingLevel; i++) {
         displayString += TAB;
      }
      displayString += fdd.getTagNum();
      displayString += (fdd.getTagName().isEmpty() ? "" : (" (" + fdd.getTagName() + ")"));
      displayString += " = " + fdd.getTagVal();
      displayString += (fdd.getTagDef().isEmpty() ? "" : (" (" + fdd.getTagDef() + ") "));
      displayString += "\n";
      return displayString;
   }

   private String getDelimiterLine(int nestingLevel, int sortOption) {
      if (sortOption > 0) {
         return "";
      }
      String displayString = "";
      for (int i = 0; i < nestingLevel; i++) {
         displayString += TAB;
      }
      displayString += "------------------------------------\n";
      return displayString;
   }

   private String getCustomTag8(String fixMessage, int customDictOption) {
      String returnMsg = fixMessage;
      switch (customDictOption) {
         case 1: // Fidessa
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
      String testMsg =
         "8=FIX.4.2^A9=0059^A35=0^A49=CANTORINTL^A56=FLEXASIA^A34=460^A52=20230525-04:06:44^A10=035^A";
      testMsg = testMsg.replaceAll("\\r\\n|\\r|\\n", "");
      testMsg = testMsg.replaceAll("\\^A", "\u0001");
      testMsg = testMsg.replaceFirst("8=FIX.+?\u0001", "8=FIX.4.4Fid\u0001");
      System.out.println(testMsg);
   }
}
