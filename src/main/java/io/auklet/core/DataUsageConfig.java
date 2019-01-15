package io.auklet.core;

import net.jcip.annotations.Immutable;

/**
 * <p>Implementations of this interface represent a <i>data usage config</i> object, which describes
 * to the Auklet agent what limits should be imposed on the amount of data the Auklet agent emits.</p>
 */
@Immutable
public final class DataUsageConfig {

    private final long emissionPeriod;
    private final long storageLimit;
    private final long cellularDataLimit;
    private final int cellularPlanDate;

    /**
     * <p>Constructor.</p>
     *
     * @param emissionPeriod the emission period.
     * @param storageLimit the storage limit.
     * @param cellularDataLimit the cellular data limit.
     * @param cellularPlanDate the cellular plan date.
     */
    public DataUsageConfig(long emissionPeriod, long storageLimit, long cellularDataLimit, int cellularPlanDate) {
        this.emissionPeriod = emissionPeriod;
        this.storageLimit = storageLimit;
        this.cellularDataLimit = cellularDataLimit;
        this.cellularPlanDate = cellularPlanDate;
    }

    /**
     * <p>Returns the emission period.</p>
     *
     * @return the emission period.
     */
    public long getEmissionPeriod() {
        return this.emissionPeriod;
    }

    /**
     * <p>Returns the storage limit.</p>
     *
     * @return the storage limit.
     */
    public long getStorageLimit() {
        return this.storageLimit;
    }

    /**
     * <p>Returns the cellular data limit.</p>
     *
     * @return the cellular data limit.
     */
    public long getCellularDataLimit() {
        return this.cellularDataLimit;
    }

    /**
     * <p>Returns the cellular plan date.</p>
     *
     * @return the cellular plan date.
     */
    public int getCellularPlanDate() {
        return this.cellularPlanDate;
    }

}
