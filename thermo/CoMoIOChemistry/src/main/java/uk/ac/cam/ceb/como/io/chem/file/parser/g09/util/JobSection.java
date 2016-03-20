package uk.ac.cam.ceb.como.io.chem.file.parser.g09.util;

import java.util.List;

/**
 *
 * @author pb556
 */
public class JobSection extends StringList {

    private final boolean normalTerminated;
    private final int order;

    public JobSection(List<String> lines, int jobOrder, boolean normalTerminated) {
        super(lines);
        this.order = jobOrder;
        this.normalTerminated = normalTerminated;
    }

    /**
     * Get the value of order
     *
     * @return the value of order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Get the value of normalTerminated
     *
     * @return the value of normalTerminated
     */
    public boolean getNormalTerminated() {
        return normalTerminated;
    }
}
