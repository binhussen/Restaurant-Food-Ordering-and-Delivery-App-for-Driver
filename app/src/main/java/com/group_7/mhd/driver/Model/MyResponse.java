package com.group_7.mhd.driver.Model;

import java.util.List;


public class MyResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
