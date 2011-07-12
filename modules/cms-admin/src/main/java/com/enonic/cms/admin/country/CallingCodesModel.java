package com.enonic.cms.admin.country;

import java.util.List;

/**
 * Class represents response for CallingCodeResource,
 * it then will be converted to JSON
 *
 * @author Viktar Fiodarau
 * @see CallingCodeResource
 */
public class CallingCodesModel
{


    private int total;

    public List<CallingCodeModel> codes;

    public List<CallingCodeModel> getCodes()
    {
        return codes;
    }

    public void setCodes( List<CallingCodeModel> codes )
    {
        this.codes = codes;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal( int total )
    {
        this.total = total;
    }


}
