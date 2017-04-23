package com.yiran.xingtian.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Xingtian on 14/02/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardIndexVO {
    private int dbIndex;

    private int tableIndex;
}
