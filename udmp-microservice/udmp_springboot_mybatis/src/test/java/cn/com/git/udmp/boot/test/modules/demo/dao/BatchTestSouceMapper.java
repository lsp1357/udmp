package cn.com.git.udmp.boot.test.modules.demo.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import cn.com.git.udmp.boot.test.modules.demo.entity.BatchTestSouce;



@Mapper
public interface BatchTestSouceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Long numA);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    int insert(BatchTestSouce record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    int insertSelective(BatchTestSouce record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    BatchTestSouce selectByPrimaryKey(Long numA);
    
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(BatchTestSouce record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table UDMP.T_UDMP_BATCH_TEST_SOURCE
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(BatchTestSouce record);
    
    
    /**
     * 查询多条记录
     * @param record
     * @return
     */
    List<BatchTestSouce> findList(BatchTestSouce record);
    
    
    /**
     * 根据索引查询区间
     * @param startNum
     * @param endNum
     * @return
     */
    List<Map> findIndexSection(long startNum,long endNum);
    
}