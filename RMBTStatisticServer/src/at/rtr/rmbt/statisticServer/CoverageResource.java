/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 * Copyright 2013-2014 Rundfunk und Telekom Regulierungs-GmbH (RTR-GmbH)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.rtr.rmbt.statisticServer;

import at.rtr.rmbt.statisticServer.opendata.dto.CoverageDTO;
import at.rtr.rmbt.statisticServer.opendata.dto.CoveragesDTO;
import at.rtr.rmbt.statisticServer.opendata.dto.OpenTestDetailsDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Statistics for internal purpose
//breaks the mvvm-pattern
@Api(value="/coverage")
public class CoverageResource extends ServerResource
{
    private final String webRoot = "http://www.netztest.at/en";
    @Path("/coverage")
    @ApiOperation(value = "Get coverage information",
            notes = "Get coverage information for a specific point",
            response = CoverageDTO.class,
            httpMethod = "GET",
            nickname = "coverage")
    @GET
    @Get("json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lat", value = "Mandatory. Latitude of the client position.", dataType = "float", example = "18.2345", paramType = "query", required = true),
            @ApiImplicitParam(name = "long", value = "Mandatory. Longitude of the client position.", dataType = "float", example = "43.1234", paramType = "query", required = true)
    })
    @Post("json")
    public String request(final String entity) {
        addAllowOrigin();

        double lat = 0, lng = 0;
        //parameters
        final Form getParameters = getRequest().getResourceRef().getQueryAsForm();
        try {


            if (getParameters.getNames().contains("lat")) {
                lat = Double.parseDouble(getParameters.getFirstValue("lat"));
                if (lat > 90 || lat < -90) {
                    throw new NumberFormatException();
                }
            }
            if (getParameters.getNames().contains("long")) {
                lng = Double.parseDouble(getParameters.getFirstValue("long"));
                if (lng > 180 || lng < -180) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            return "invalid parameters";
        }

        long startTime = System.currentTimeMillis();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT " +
                    " cov_mno_fn.operator," + //varchar
                    " cov_mno_fn.reference," +  //varchar
                    " cov_mno_fn.license," + //text
                    " cov_mno_fn.rfc_date last_updated," + //text
                    " cov_mno_fn.raster," + //varchar
                    " round(cov_mno_fn.dl_max /1000) downloadKbitMax," +  //bigint
                    " round(cov_mno_fn.ul_max /1000) uploadKbitMax," + //bigint
                    " round(cov_mno_fn.dl_normal /1000) downloadKbitNormal," + //bigint
                    " round(cov_mno_fn.ul_normal/1000) uploadKbitNormal," + //bigint
                    " cov_mno_fn.technology," +
                    " ST_AsGeoJSON(ST_Transform(geom,4326)) geoJson" + //varchar
                    " from atraster " +
                    " left join cov_mno_fn on raster=id " +
                    " where cov_mno_fn.raster is not null AND" +
                    " ST_intersects((ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326),3035)),geom);");
            ps.setDouble(1, lng);
            ps.setDouble(2, lat);
            //System.out.println(ps);
            if (!ps.execute())
                return null;
            ResultSet rs = ps.getResultSet();

            BeanListHandler<CoverageDTO> handler = new BeanListHandler<>(CoverageDTO.class, new BasicRowProcessor(new GenerousBeanProcessor()));
            List<CoverageDTO> results = handler.handle(rs);
            CoveragesDTO result = new CoveragesDTO();
            result.setCoverages(results);
            ps.close();

            long elapsedTime = System.currentTimeMillis() - startTime;
            result.setDurationMs(elapsedTime);

            ObjectMapper om = new ObjectMapper();
            om.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            return (om.writer().writeValueAsString(result));
        } catch (SQLException | JsonProcessingException throwables) {
            throwables.printStackTrace();
        }

        return "";
    }
}
