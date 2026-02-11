package com.pedrozc90.epcs.objects;

/**
 * @param partitionValue p = partition
 * @param m              m = company prefix bits
 * @param l              l = company prefix length
 * @param n              n = item reference bits
 * @param digits         digits = item reference length
 */
public record TableItem(int partitionValue, int m, int l, int n, int digits) {
    // ignore
}
