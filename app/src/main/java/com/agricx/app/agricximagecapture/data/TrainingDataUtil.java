package com.agricx.app.agricximagecapture.data;

import com.agricx.app.agricximagecapture.utility.AppConstants.DefectType;

import java.util.ArrayList;

public class TrainingDataUtil {

    public static class BaseDefect {
        private String defectId;
        private String defectFullName;
        private String defectShortName;
        private String defectTypeName;

        BaseDefect(String defectId, String defectFullName, String defectTypeName, String defectShortName) {
            this.defectId = defectId;
            this.defectFullName = defectFullName;
            this.defectShortName = defectShortName;
            this.defectTypeName = defectTypeName;
        }

        public String getDefectId() {
            return defectId;
        }

        public String getDefectFullName() {
            return defectFullName;
        }

        public String getDefectShortName() {
            return defectShortName;
        }

        public String getDefectTypeName() {
            return defectTypeName;
        }
    }

    // To add new Defect
    // If Type is also new - add new type in the `DefectType Interface` and add a new BaseDefect object in the defectList
    // If Type is existing - Simply add a new BaseDefect object in the defectList
    public static ArrayList<BaseDefect> getDefectList() {
        ArrayList<BaseDefect> defectList = new ArrayList<>();
        String typeHealthyName = DefectType.HEALTHY.getName();
        String typeExternalDefectName = DefectType.EXTERNAL_DEFECT.getName();
        String typeInternalDefectName = DefectType.INTERNAL_DEFECT.getName();
        
        defectList.add(new BaseDefect("101", "Whole Potatoes with Skin", typeHealthyName, "Whole"));
        defectList.add(new BaseDefect("102", "Cut Potatoes", typeHealthyName, "Cut"));
        defectList.add(new BaseDefect("201", "Bruisings", typeExternalDefectName, "Bruisings"));
        defectList.add(new BaseDefect("202", "Sprouting", typeExternalDefectName, "Sprouting"));
        defectList.add(new BaseDefect("203", "Greening", typeExternalDefectName, "Greening"));
        defectList.add(new BaseDefect("204", "Growth Crack", typeExternalDefectName, "GrowthCrack"));
        defectList.add(new BaseDefect("205", "Irregular Shape", typeExternalDefectName, "IrregularShape"));
        defectList.add(new BaseDefect("206", "Dry Rotted", typeExternalDefectName, "DryRot"));
        defectList.add(new BaseDefect("207", "Wet Rotted", typeExternalDefectName, "WetRot"));
        defectList.add(new BaseDefect("208", "Mechanical Damage", typeExternalDefectName, "MechDamage"));
        defectList.add(new BaseDefect("209", "Scab", typeExternalDefectName, "Scab"));
        defectList.add(new BaseDefect("210", "Stem End", typeExternalDefectName, "StemEnd"));
        defectList.add(new BaseDefect("211", "Insect Damage", typeExternalDefectName, "InsectDamage"));
        defectList.add(new BaseDefect("301", "Black Spots", typeInternalDefectName, "BlackSpots"));
        defectList.add(new BaseDefect("302", "Brown Spots", typeInternalDefectName, "BrownSpots"));
        defectList.add(new BaseDefect("303", "Hollow Heart", typeInternalDefectName, "HollowHeart"));
        defectList.add(new BaseDefect("304", "Internal Sprouting", typeInternalDefectName, "InternalSprouting"));

        return defectList;
    }

    public static ArrayList<BaseDefect> getDefectListFromDefectType(DefectType defectType) {
        ArrayList<BaseDefect> defectList = new ArrayList<>();
        for (BaseDefect baseDefect : getDefectList()) {
            if (baseDefect.getDefectTypeName().equalsIgnoreCase(defectType.getName())) {
                defectList.add(baseDefect);
            }
        }
        return defectList;
    }

    public static class BaseVariety {
        private String varietyId;
        private String varietyFullName;
        private String varietyShortName;

        BaseVariety(String varietyId, String varietyFullName, String varietyShortName) {
            this.varietyId = varietyId;
            this.varietyFullName = varietyFullName;
            this.varietyShortName = varietyShortName;
        }

        public String getVarietyId() {
            return varietyId;
        }

        public String getVarietyFullName() {
            return varietyFullName;
        }

        public String getVarietyShortName() {
            return varietyShortName;
        }
    }

    // To add a new variety simply add a new BaseVariety in the varietyList
    public static ArrayList<BaseVariety> getVarietyList() {
        ArrayList<BaseVariety> varietyList = new ArrayList<>();
        varietyList.add(new BaseVariety("1", "Chipsona - I", "Chipsona1"));
        varietyList.add(new BaseVariety("2", "Chipsona - III", "Chipsona3"));
        varietyList.add(new BaseVariety("3", "Kufri Bahar (3797)", "Kufri3797"));
        varietyList.add(new BaseVariety("4", "Kufri Jyoti", "KufriJyoti"));
        varietyList.add(new BaseVariety("5", "Lady Rosetta (LR)", "LR"));
        varietyList.add(new BaseVariety("6", "Lal Lauvkar (LL)", "LL"));
        varietyList.add(new BaseVariety("7", "Santana", "Santana"));
        return varietyList;
    }
}
