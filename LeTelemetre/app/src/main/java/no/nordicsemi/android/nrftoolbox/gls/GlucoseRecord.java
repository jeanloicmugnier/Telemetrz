package no.nordicsemi.android.nrftoolbox.gls;

import java.util.Calendar;

public class GlucoseRecord {
    public static final int UNIT_kgpl = 0;
    public static final int UNIT_molpl = 1;
    protected MeasurementContext context;
    protected float glucoseConcentration;
    protected int sampleLocation;
    protected int sequenceNumber;
    protected int status;
    protected Calendar time;
    protected int timeOffset;
    protected int type;
    protected int unit;

    public static class MeasurementContext {
        public static final int UNIT_kg = 0;
        public static final int UNIT_l = 1;
        protected float HbA1c;
        protected int carbohydrateId;
        protected float carbohydrateUnits;
        protected int exerciseDurtion;
        protected int exerciseIntensity;
        protected int health;
        protected int meal;
        protected int medicationId;
        protected float medicationQuantity;
        protected int medicationUnit;
        protected int tester;
    }
}
