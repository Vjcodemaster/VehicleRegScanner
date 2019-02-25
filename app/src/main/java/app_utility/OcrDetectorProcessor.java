/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app_utility;

import android.util.Log;
import android.util.SparseArray;

import com.autochip.vehiclenoscanner.CameraFragment;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    //private ArrayList<String> alCardInfoSeparated = new ArrayList<>();

    private String s1;
    private boolean isNamePresent = false;
    private boolean isDesignationPresent = false;
    String[] saAddressChecker = {"website", "web", "address"};
    String[] saEmailChecker = {"mail-", "Email", ":", "Email:", "al", "ail-"};

    ArrayList<String> alData = new ArrayList<>();
    String sResult;
    boolean is2Added = false;
    boolean is4Added = false;
    boolean is7Added = false;

    public GraphicOverlay<OcrGraphic> graphicOverlay;
    private HashMap<String, String> hmCardData = new HashMap<>();

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        graphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        graphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        if (items.size() >= 1) {
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                if (item != null && item.getValue() != null) {
                    Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                    //hmCardData.add(item.getValue());
                    //OcrGraphic graphic = new OcrGraphic(graphicOverlay, item);
                    //graphicOverlay.add(graphic);
                    //separateeAllInfo(item.getValue());
                    if (item.getValue().length() >= 1)
                        separateData(item.getValue());
                }
            }
            if (alData.size() >= 2) {
                //validateInfo();
                String sVehicleNumber = android.text.TextUtils.join("", alData);
                hmCardData.put("vehicle_no", sVehicleNumber);
                CameraFragment.mListener.onFragmentMessage("IMAGE_PROCESS_COMPLETE", null, hmCardData);
            } else {
                hmCardData.clear();
            }
        }
    }

    private void separateData(String sText) {
        String[] lines = sText.split(Objects.requireNonNull(System.getProperty("line.separator")));
        StringBuilder sb;
        //alData.addAll(Arrays.asList(lines));

        for (String s : lines) {
            if (s.length() == 2) {
                if (!alData.contains(s) && !is2Added) {
                    alData.add(s);
                    is2Added = true;
                }
            } else if (s.length() == 4 && s.matches("[0-9]+") && !is4Added) {
                if (!alData.contains(s)) {
                    alData.add(s);
                    is4Added = true;
                }
            } else if (s.length() <= 7 && !is7Added) {
                if (!alData.contains(s)) {
                    alData.add(s);
                    is7Added = true;
                }
            } else if (s.length() <= 14 && s.contains(" ")) {
                if (!alData.contains(s))
                    alData.add(s);
            }
        }

        /*for (String s : lines) {
            if (lines.length == 2) {
                String sName = lines[0];
                String sDesignation = lines[1];
                if (isAlpha(sName) && isAlpha(sDesignation)) {
                    if (sName.length() >= 3) {
                        hmCardData.put("name", sName);
                        isNamePresent = true;
                    }
                    if (sDesignation.length() >= 3) {
                        hmCardData.put("designation", sDesignation);
                        isDesignationPresent = true;
                    }
                }
            }
            if (!isNamePresent && isAlpha(s)) {
                hmCardData.put("name", s);
            } else if (!isDesignationPresent && isAlpha(s)) {
                hmCardData.put("designation", s);
            } else if (isNumber(s)) {
                String[] saNumbers = null;
                if (s.contains(","))
                    saNumbers = s.split(",");
                else if (s.contains("/"))
                    saNumbers = s.split("/");

                if (saNumbers != null && saNumbers.length >= 1) {
                    for (String sNumber : saNumbers) {
                        if (matchesLengthCondition(sNumber)) {
                            s1 = sNumber.replaceAll("[^0-9]", "").trim();
                            if (s1.length() == 12) {
                                s1 = "+" + s1.trim();
                            }
                            if (hmCardData.containsKey("number")) {
                                sb = new StringBuilder();
                                sb.append(hmCardData.get("number")).append(",");
                                sb.append(s1);
                                hmCardData.put("number", sb.toString());
                            } else {
                                hmCardData.put("number", s1);
                            }
                        }
                    }
                } else {
                    if (matchesLengthCondition(s)) {
                        s1 = s.replaceAll("[^0-9]", "").trim();
                        if (s1.length() == 12) {
                            s1 = "+" + s1.trim();
                        }
                        if (hmCardData.containsKey("number")) {
                            sb = new StringBuilder();
                            sb.append(hmCardData.get("number")).append(",");
                            sb.append(s1);
                            hmCardData.put("number", sb.toString());
                        } else {
                            hmCardData.put("number", s1);
                        }
                    }
                }
            } else if (s.contains("@")) {
                String[] saEmail = null;
                if (s.contains(","))
                    saEmail = s.split(",");
                else if (s.contains("/"))
                    saEmail = s.split("/");

                if (saEmail != null && saEmail.length >= 1) {
                    for (String sEmail : saEmail) {
                        if (hmCardData.containsKey("email")) {
                            sb = new StringBuilder();
                            sb.append(hmCardData.get("email"));
                            sb.append(",");
                            sb.append(prefixChecker("EMAIL", sEmail.trim(), saEmailChecker));
                            hmCardData.put("email", sb.toString());
                        } else {
                            hmCardData.put("email", prefixChecker("EMAIL", sEmail.trim(), saEmailChecker));
                        }
                    }
                } else {
                    hmCardData.put("email", prefixChecker("EMAIL", s.trim(), saEmailChecker));
                }
            } else if (s.contains("w.")) {
                String[] saWebsite = null;
                if (s.contains(","))
                    saWebsite = s.split(",");
                else if (s.contains("/"))
                    saWebsite = s.split("/");

                if (saWebsite != null && saWebsite.length >= 1) {
                    for (String sWebsite : saWebsite) {
                        if (hmCardData.containsKey("website")) {
                            sb = new StringBuilder();
                            sb.append(hmCardData.get("website"));
                            sb.append(",");
                            sb.append(prefixChecker("WEBSITE", sWebsite.trim(), null));
                            hmCardData.put("website", sb.toString());
                        } else {
                            hmCardData.put("website", prefixChecker("WEBSITE", sWebsite.trim(), null));
                        }
                    }
                } else {
                    hmCardData.put("website", prefixChecker("WEBSITE", s.trim(), null));
                }
            } else if (matchesPinCondition(s) || isAddress(s)) {
                if (hmCardData.containsKey("address")) {
                    sb = new StringBuilder();
                    sb.append(hmCardData.get("address"));
                    sb.append(",");
                    sb.append(s);
                    hmCardData.put("address", sb.toString());
                } else
                    hmCardData.put("address", s);
            }
        }*/
    }

    /*if (saWebsite != null && saWebsite.length >= 1) {
        for (String sWebsite : saWebsite) {
            if (hmCardData.containsKey("website")) {
                sb = new StringBuilder();
                sb.append(hmCardData.get("website"));
                sb.append(",");
                if(!sWebsite.startsWith("w.") || !sWebsite.startsWith("ww.")){
                    int dotIndex = sWebsite.indexOf(".");
                    String sFinalWebsite = "www." + sWebsite.substring(dotIndex, sWebsite.length()-1);
                }
                sb.append(sWebsite);
                hmCardData.put("website", sb.toString());
            } else {
                hmCardData.put("website", sWebsite);
            }
        }
    } else {
        hmCardData.put("website", s);
    }*/

    /*private boolean isAlphaLoop(String name) {
        for (String s : name.split(",")) {
            if (!s.matches("[ A-Za-z]+")) {
                return false;
            }
        }
        return true;
    }*/
    /*

     */
    private String prefixChecker(String sCase, String sToCheck, String[] saChecker) {
        switch (sCase) {
            case "EMAIL":
                for (String checker : saChecker) {
                    if (sToCheck.equals(checker)) {
                        return checker.substring(checker.length() - 1, sToCheck.length() - 1);
                    }
                    /*if(i == saChecker.length-1){
                        return sToCheck;
                    }*/
                }
                break;
            case "WEBSITE":
                if (!sToCheck.startsWith("www.")) {
                    int dotIndex = sToCheck.indexOf(".");
                    return "www" + sToCheck.substring(dotIndex, sToCheck.length());
                }
                break;
        }

        return sToCheck;
    }

    private boolean isAlpha(String name) {
        return name.matches("[ A-Za-z]+"); //^[ A-Za-z]+$
    }

    private boolean isNumber(String sNumber) {
        s1 = sNumber.replaceAll("[^0-9]", "").trim();
        return s1.length() > 7;
    }

    private boolean matchesLengthCondition(String sNumber) {
        s1 = sNumber.replaceAll("[^0-9]", "").trim();
        return s1.length() > 7 && s1.length() < 13;
    }

    private boolean matchesPinCondition(String sNumber) {
        s1 = sNumber.replaceAll("[^0-9]", "").trim();
        return s1.length() > 4 && s1.length() < 7;
    }


    private boolean isAddress(String sAddress) {
        //String[] saAddressSpace = sAddress.split(" ");
        String[] saAddress = sAddress.split(",");
        if (saAddress.length >= 2) {
            for (String sAddressCheck : saAddress) {
                if (sAddressCheck.contains("@") || sAddressCheck.contains("&"))
                    return false;
                if (matchesLengthCondition(sAddressCheck)) {
                    String sN = hmCardData.get("name");
                    String sD = hmCardData.get("designation");

                    if (sAddressCheck.equals(sN) || sAddressCheck.equals(sD))
                        return false;
                }
            }
        } else {
            return false;
        }
        /*else if(saAddress.length ==1){ //if anything goes wrong in identifying address then remove else if statement
            String sN = hmCardData.get("name");
            String sD = hmCardData.get("designation");

            if(saAddress[0].equals(sN) && saAddress[0].equals(sD) && saAddress[0].contains("&"))
                return  false;
        }*/
        return true;
    }

    /*public static boolean stringContainsItemFromList(String inputStr, String[] items)
    {
        for (String item : items) {
            if (inputStr.contains(item)) {
                return true;
            }
        }
        return false;
    }*/

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        graphicOverlay.clear();
    }
}
