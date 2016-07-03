package com.watermarkcreator.util;

/**
 * Created by b_ashish on 30-Jun-16.
 */

public abstract class BaseWaterMarkBuilder<T extends BaseWaterMarkBuilder<T>> {

    protected abstract T self();

}
