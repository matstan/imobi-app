package com.imobiapp.parser.dm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author matics.
 */
public class AdDataHistory {
    public long lastSynced;
    public String hash;
    public boolean seen;

    public List<AdData> adDataHistory = new ArrayList<>();
}
