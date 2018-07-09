<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xtt="urn:com.workday/xtt"
    xmlns:etv="urn:com.workday/etv"
    xmlns:ws="urn:com.workday/workersync"
    xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
    exclude-result-prefixes="xs ws xtt saxon"
    version="3.0">
    <xsl:variable name="TotalRecordCount" select="0" saxon:assignable="yes" />
    <xsl:template match="/ws:Worker_Sync">
        
        <FILE xtt:separator="&#xd;&#xa;" xtt:align="left" xtt:severity="warning"
            xtt:paddingCharacter=" ">
            <HEADER xtt:startTag="HDR">
                <CLIENT_NAME xtt:fixedLength="30">CloudDevops</CLIENT_NAME>
                <FILE_CREATION_DATE xtt:fixedLength="8"><xsl:value-of select="format-date(current-date(),'[Y0123][M01][D01]')"/></FILE_CREATION_DATE>
                <FILE_VERSION_NUMBER xtt:fixedLength="5">001.0</FILE_VERSION_NUMBER>
                <FILE_LAYOUT_NAME_NAME xtt:fixedLength="20">AllBenefits</FILE_LAYOUT_NAME_NAME>
                <MEMBER_ID_TYPE xtt:fixedLength="1">2</MEMBER_ID_TYPE>
                <FILLER xtt:fixedLength="6"></FILLER>
                <CHANGES_ONLY xtt:fixedLength="1">2</CHANGES_ONLY>
                <FILLER xtt:fixedLength="15"></FILLER>
                <FILLER xtt:fixedLength="1"></FILLER>
                <FILLER xtt:fixedLength="2"></FILLER>
            </HEADER>
            <xsl:apply-templates select="ws:Worker"/>
            <TRAILER xtt:startTag="TRL">
                <RECORD_COUNT xtt:fixedLength="10" xtt:align="right" xtt:paddingCharacter="0">
                    <xsl:value-of select="$TotalRecordCount" />
                </RECORD_COUNT>    
            </TRAILER>
        </FILE>
    </xsl:template>
    <xsl:template match="ws:Worker">
        <xsl:if test="ws:Additional_Information/ws:Ongoing_Long_Term_Disability_Count=0 and ws:Status/ws:Retired='false'">
            <saxon:assign name="TotalRecordCount">
                <xsl:value-of select="$TotalRecordCount + 1"/>
            </saxon:assign>
            <MEMBER_RECORD>
            <RECORD_TYPE xtt:fixedLength="3">M</RECORD_TYPE>
            <CLIENT_NUMBER xtt:fixedLength="7">0107924</CLIENT_NUMBER>
            <ACTION xtt:fixedLength="3">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Staffing_Event='TRM'">
                        <xsl:value-of select="ws:Status/ws:Staffing_Event"/>
                    </xsl:when>
                    <xsl:when test="ws:Status/ws:Employee_Status='Terminated'">
                        <xsl:text>TRM</xsl:text>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </ACTION> 
            <EFF_DATE xtt:fixedLength="8">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Staffing_Event='TRM' or ws:Status/ws:Employee_Status='Terminated'">
                        <xsl:value-of select="format-date(ws:Status/ws:Termination_Date,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                    <xsl:when test="ws:Status/ws:Staffing_Event='HIR'">
                        <xsl:value-of select="format-date(ws:Status/ws:Hire_Date,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                    <xsl:when test="ws:Status/ws:Staffing_Event='RFL'">
                        <xsl:value-of select="format-date(ws:Additional_Information/ws:Actual_Last_Day_of_Leave_Plus_1,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                    <xsl:when test="ws:Status/ws:Staffing_Event!=''">
                        <xsl:value-of select="format-date(ws:Status/ws:Staffing_Event_Date,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                    <xsl:when test="ws:Personal/ws:Name_Data/ws:First_Name/@ws:PriorValue!='' or ws:Personal/ws:Name_Data/ws:Last_Name/@ws:PriorValue!=''">
                        <xsl:value-of select="format-date(ws:Additional_Information/ws:CF_Worker_Legal_Name_Eff_Date,'[Y0123][M01][D01]')"/>
                    </xsl:when>  
                    <xsl:when test="(ws:Status/ws:Employee_Status/@ws:PriorValue='OnLeave' or exists(ws:Additional_Information/ws:Actual_Last_Day_of_Leave_Plus_1/@ws:PriorValue)) and ws:Additional_Information/ws:Actual_Last_Day_of_Leave_Plus_1!=''">
                        <xsl:value-of select="format-date(ws:Additional_Information/ws:Actual_Last_Day_of_Leave_Plus_1,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                   <xsl:otherwise>
                       <!--     <xsl:value-of select="format-date(ws:Status/ws:Hire_Date,'[Y0123][M01][D01]')"/> -->
                       <xsl:value-of select="format-date(current-date(), '[Y0123][M01][D01]')"/>
                   </xsl:otherwise>
                </xsl:choose>
            </EFF_DATE>
            <CERTIFICATE_NBR xtt:fixedLength="9"></CERTIFICATE_NBR>
                <PAYROLL_ID xtt:fixedLength="10"><xsl:value-of select="ws:Summary/ws:Employee_ID"/></PAYROLL_ID>
            <FILLER xtt:fixedLength="9"></FILLER>
            <FIRST_NAME xtt:fixedLength="30"><xsl:value-of select="ws:Personal/ws:Name_Data/ws:First_Name"/></FIRST_NAME>
            <FILLER xtt:fixedLength="6"></FILLER>
            <LAST_NAME xtt:fixedLength="30"><xsl:value-of select="ws:Personal/ws:Name_Data/ws:Last_Name"/></LAST_NAME>
            <DOB xtt:fixedLength="8"><xsl:value-of select="format-date(ws:Personal/ws:Birth_Date,'[Y0123][M01][D01]')"/></DOB>
            <GENDER xtt:fixedLength="1">
                <xsl:choose>
                    <xsl:when test="ws:Personal/ws:Gender='M'">
                        <xsl:text>1</xsl:text>
                    </xsl:when>
                    <xsl:when test="ws:Personal/ws:Gender='F'">
                        <xsl:text>2</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            
            </GENDER>
            <LANGUAGE xtt:fixedLength="1">
                <xsl:choose>
                    <xsl:when test="contains(ws:Additional_Information/ws:Language,'English')">
                        <xsl:text>1</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>2</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </LANGUAGE>
            <SMOKE_STATUS xtt:fixedLength="1"></SMOKE_STATUS>
            <PLAN xtt:fixedLength="3">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Retired='true'">
                        <xsl:text>R1</xsl:text>
                    </xsl:when>
                    <xsl:when test="ws:Additional_Information/ws:Ongoing_Long_Term_Disability_Count>0">
                        <xsl:text>DA</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>AA</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </PLAN>
            <LOCATION xtt:fixedLength="3">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Retired='true'">
                        <xsl:text>100</xsl:text>
                    </xsl:when>
                    <xsl:when test="ws:Additional_Information/ws:Ongoing_Long_Term_Disability_Count>0">
                        <xsl:text>030</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>010</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </LOCATION>
            <CLASS xtt:fixedLength="3">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Retired='true'">
                        <xsl:text>100</xsl:text>
                    </xsl:when>
                    <xsl:when test="ws:Additional_Information/ws:Ongoing_Long_Term_Disability_Count>0">
                        <xsl:text>030</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>010</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </CLASS>
                <COST_CENTER1 xtt:fixedLength="10"><xsl:value-of select="ws:Additional_Information/ws:Cost_Center_1"/></COST_CENTER1>
            <COST_CENTER2 xtt:fixedLength="10"></COST_CENTER2>
            <COST_CENTER3 xtt:fixedLength="10"></COST_CENTER3>
                <DOE xtt:fixedLength="8"><xsl:value-of select="format-date(ws:Status/ws:Continuous_Service_Date,'[Y0123][M01][D01]')"/></DOE>
            <RESIDENCE_PROVINCE xtt:fixedLength="2"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='HOME']/ws:Region"/></RESIDENCE_PROVINCE>
            <WORK_PROVINCE xtt:fixedLength="2"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='WORK']/ws:Region"/></WORK_PROVINCE>
            <FILLER xtt:fixedLength="2"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <RETIREMENT_DATE xtt:fixedLength="8"></RETIREMENT_DATE>
            <OCCUPATION xtt:fixedLength="1"></OCCUPATION>
            <DATE_OF_DEATH xtt:fixedLength="8"></DATE_OF_DEATH>
            <SUPERVISOR_STATUS xtt:fixedLength="1"></SUPERVISOR_STATUS>
            <TAX_EXEMPT  xtt:fixedLength="1"></TAX_EXEMPT>
            <FILLER xtt:fixedLength="1"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <FILLER xtt:fixedLength="8"></FILLER>
            <FILLER xtt:fixedLength="1"></FILLER>
                </MEMBER_RECORD>
        
            <saxon:assign name="TotalRecordCount">
           <xsl:value-of select="$TotalRecordCount + 1"/>
       </saxon:assign>
       <SAL_RECORD>    
       <RECORD_TYPE xtt:fixedLength="3">S</RECORD_TYPE>
            <CLIENT_NUMBER xtt:fixedLength="7">0107924</CLIENT_NUMBER>
            <ACTION xtt:fixedLength="3"></ACTION>
           <EFF_DATE xtt:fixedLength="8">
           <xsl:choose>
               <xsl:when test="ws:Compensation/ws:Operation='NONE'">
                   <xsl:value-of select="format-date(current-date(), '[Y0123][M01][D01]')"/>
               </xsl:when>
               <xsl:otherwise>
                   <xsl:value-of select="format-date(ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Effective_Date,'[Y0123][M01][D01]')"/>
               </xsl:otherwise>
           </xsl:choose>
            </EFF_DATE>
            <CERTIFICATE_NBR xtt:fixedLength="9"></CERTIFICATE_NBR>
            <PAYROLL_ID xtt:fixedLength="10"><xsl:value-of select="ws:Summary/ws:Employee_ID"/></PAYROLL_ID>
            <FILLER xtt:fixedLength="9"></FILLER>
            <SAL_DESC_CODE xtt:fixedLength="2">01</SAL_DESC_CODE>
            <SAL_BASIS xtt:fixedLength="1">
                <xsl:text>1</xsl:text>
             <!--  
                <xsl:variable name="salbasis">
                    <xsl:choose>
                        <xsl:when test="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency!=''">
                            <xsl:value-of select="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency"/>
                        </xsl:when>
                        <xsl:when test="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency/@ws:PriorValue!=''">
                            <xsl:value-of select="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency/@ws:PriorValue"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="ws:Additional_Information/ws:Base_Pay_Frequency_As_of_Termination_Date"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable> 
                
               <xsl:choose>
                   <xsl:when test="$salbasis='Annual'">
                       <xsl:text>1</xsl:text>
                   </xsl:when>
                   <xsl:when test="$salbasis='Semimonthly'">
                       <xsl:text>6</xsl:text>
                   </xsl:when>
                   <xsl:when test="$salbasis='Monthly'">
                       <xsl:text>2</xsl:text>
                   </xsl:when>
                   <xsl:when test="$salbasis='Weekly'">
                       <xsl:text>4</xsl:text>
                   </xsl:when>
                   <xsl:when test="$salbasis='Biweekly'">
                       <xsl:text>3</xsl:text>
                   </xsl:when>
                   <xsl:when test="$salbasis='Hourly'">
                       <xsl:text>5</xsl:text>
                   </xsl:when>
                   <xsl:otherwise>
                       <xsl:text>0</xsl:text>
                   </xsl:otherwise>
               </xsl:choose>
               --> 
            </SAL_BASIS>
            <SAL_AMOUNT xtt:fixedLength="11" xtt:align="right" xtt:paddingCharacter="0">
                <xsl:value-of select="format-number(ws:Additional_Information/ws:Benefit_Earnings,'#.00')"/>
                <!--  
                <xsl:choose>
                    <xsl:when test="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency!=''">
                        <xsl:value-of select="format-number(ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Total_Base_Pay,'#.00')"/>
                    </xsl:when>
                    <xsl:when test="ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Base_Pay_Frequency/@ws:PriorValue!=''">
                        <xsl:value-of select="format-number(ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Total_Base_Pay/@ws:PriorValue,'#.00')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="format-number(ws:Compensation[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Total_Base_Pay,'#.00')"/>
                    </xsl:otherwise>
                </xsl:choose>
                -->
            </SAL_AMOUNT>
            <HOURS_WEEK xtt:fixedLength="8" xtt:align="right" xtt:paddingCharacter="0">
                <xsl:value-of select="format-number(ws:Position[ws:Operation!='REMOVE' or not(ws:Operation)]/ws:Scheduled_Weekly_Hours,'#####.00')"/>
            </HOURS_WEEK>
           </SAL_RECORD>
        <saxon:assign name="TotalRecordCount">
                 <xsl:value-of select="$TotalRecordCount + 1"/>
             </saxon:assign>
             <ADDR_RECORD>
             <RECORD_TYPE xtt:fixedLength="3">A</RECORD_TYPE>
            <CLIENT_NUMBER xtt:fixedLength="7">0107924</CLIENT_NUMBER>
            <FILLER xtt:fixedLength="3"></FILLER>
            <EFF_DATE xtt:fixedLength="8">
                <xsl:choose>
                    <xsl:when test="ws:Status/ws:Staffing_Event='HIR'">
                        <xsl:value-of select="format-date(ws:Status/ws:Hire_Date,'[Y0123][M01][D01]')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="format-date(current-date(), '[Y0123][M01][D01]')"/>
                    </xsl:otherwise>
                </xsl:choose>
                </EFF_DATE>
                <CERTIFICATE_NBR xtt:fixedLength="9"></CERTIFICATE_NBR>
                <PAYROLL_ID xtt:fixedLength="10"><xsl:value-of select="ws:Summary/ws:Employee_ID"/></PAYROLL_ID>
                <FILLER xtt:fixedLength="9"></FILLER>
                <ADDR_TYPE xtt:fixedLength="1">1</ADDR_TYPE>
            <STREET xtt:fixedLength="30"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='HOME' and ws:Is_Primary='true']/ws:Address_Line_Data[@ws:Type='ADDRESS_LINE_1']"/></STREET>
                 <SUITE xtt:fixedLength="30"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='HOME' and ws:Is_Primary='true']/ws:Address_Line_Data[@ws:Type='ADDRESS_LINE_2']"/></SUITE>
            <CITY xtt:fixedLength="30"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='HOME' and ws:Is_Primary='true']/ws:Municipality"/></CITY>
                <PROVINCE xtt:fixedLength="2">
                    <xsl:value-of
                        select="ws:Personal/ws:Address_Data[ws:Address_Type = 'HOME' and ws:Is_Primary = 'true']/ws:Region"
                    />
                </PROVINCE>
            <COUNRTY xtt:fixedLength="1">
                <xsl:choose>
                    <xsl:when
                        test="ws:Personal/ws:Address_Data[ws:Address_Type = 'HOME' and ws:Is_Primary = 'true']/ws:Country = 'CAN'">
                        <xsl:text>1</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>2</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </COUNRTY>
            <POSTAL xtt:fixedLength="15"><xsl:value-of select="ws:Personal/ws:Address_Data[ws:Address_Type='HOME' and ws:Is_Primary='true']/ws:Postal_Code"/></POSTAL>
            <xsl:variable name="phNumber" select="replace(ws:Personal/ws:Phone_Data[ws:Phone_Type='HOME' and ws:Is_Primary='true']/ws:Phone_Number,'-','')"/>
            <PHONE xtt:fixedLength="30">
                <xsl:choose>
                    <xsl:when test="$phNumber!=''">
                        <xsl:value-of select="concat(ws:Personal/ws:Phone_Data[ws:Phone_Type='HOME' and ws:Is_Primary='true']/ws:Phone_Area_Code,'-',substring($phNumber,1,3),'-',substring($phNumber,4))"/>
                    </xsl:when>
                    <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
                </xsl:choose>
            </PHONE>
            <WORK_EMAIL xtt:fixedLength="128"><xsl:value-of select="ws:Personal/ws:Email_Data[ws:Email_Type='WORK' and ws:Is_Primary='true']/ws:Email_Address"/></WORK_EMAIL>
                 </ADDR_RECORD>
        </xsl:if>
    </xsl:template>    
</xsl:stylesheet>