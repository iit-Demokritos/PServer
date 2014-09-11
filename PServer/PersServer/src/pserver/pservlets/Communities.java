/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */
package pserver.pservlets;

import java.sql.SQLException;
import pserver.PersServer;
import pserver.WebServer;
import pserver.algorithms.metrics.VectorMetric;
import pserver.data.DBAccess;
import pserver.data.PCommunityDBAccess;
import pserver.data.VectorMap;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;

/**
 * Contains all necessary methods for the management of Communities mode of
 * PServer.
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class Communities implements pserver.pservlets.PService {

    /**
     * Overridden method of init from {@link PService} Does nothing here.
     *
     * @param params An array of strings containing the parameters
     * @throws Exception Default Exception is thrown.
     */
    @Override
    public void init(String[] params) throws Exception {
    }

    /**
     * Returns the mime type.
     *
     * @return Returns the XML mime type from Interface {@link PService}.
     */
    @Override
    public String getMimeType() {
        return pserver.pservlets.PService.xml;
    }

    /**
     * Creates a service for Communities' mode when a command is sent to
     * PServer. The command is identified from its name and proper methods for
     * the management of this command are called. A response code is produced
     * depending on results.
     *
     * @param parameters The parameters needed for this service.
     * @param response The response string that is created.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        int respCode;
        VectorMap queryParam;
        StringBuffer respBody;

        respBody = new StringBuffer();
        queryParam = parameters;

        if (!ClientCredentialsChecker.check(dbAccess, queryParam)) {
            return PSReqWorker.REQUEST_ERR;  //no point in proceeding
        }

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String client = (String) queryParam.getVal(clntIdx);
        String clientName = client.substring(0, client.indexOf('|'));
        String clientPass = client.substring(client.indexOf('|') + 1);

        queryParam.updateVal(clientName, clntIdx);

        int comIdx = parameters.qpIndexOfKeyNoCase("com");
        if (comIdx == -1) {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command does not exist");
            return respCode;  //no point in proceeding
        }

        //recognize command encoded in request
        String com = (String) queryParam.getVal(comIdx);
        if (com.equalsIgnoreCase("addcomm")) {
            //add custom community
            respCode = comCommuAddCommunity(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("addftrgroup")) {
            //add custom feature group
            respCode = comCommuAddFeatureGroup(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("adduserassoc")) {
            //add association between two users
            respCode = comCommuAddUserAssociation(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("calcuassoc")) {
            //calculate user association
            respCode = comCommuCalculateUserAssociation(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("calcuftrassoc")) {
            //calculate feature association
            respCode = comCommuCalculateFeatureAssociation(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("makecommunities")) {
            //make user communities
            respCode = comCommuMakeCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("makeftrgroups")) {
            //make feature groups
            respCode = comCommuMakeFeatureGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("deleteusercomm")) {
            //delete user communities
            respCode = comCommuDeleteCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("deleteftrgroup")) {
            //delete feature groups
            respCode = comCommuDeleteFeatureGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusercomm")) {
            //get list with user communities names
            respCode = comCommuGetCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrgroups")) {
            //get list with featyre groups names
            respCode = comCommuGetFeatureGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getcommprofile")) {
            //get user community profile
            respCode = comCommuGetCommunityProfile(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrgroupprofile")) {
            //get feature group profile
            respCode = comCommuGetFeatureGroupProfile(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getcommusers")) {
            //get list with all users in a community
            respCode = comCommuGetCommunityUsers(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrgroupfeatures")) {
            //get list with all features in a feature group 
            respCode = comCommuGetFeatureGroupFeatures(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getusrcommu")) {
            //get list with communiy names who a user belongs
            respCode = comCommuGetUserCommunities(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getftrftrgroups")) {
            //get list with feature group names which a feature belongs
            respCode = comCommuGetFeatureFeatureGroups(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getalgorithms")) {
            //get all cluster algorithms  
            respCode = comCommuGetAlgorithms(queryParam, respBody, dbAccess);
        } else if (com.equalsIgnoreCase("getmetrics")) {
            //get all metric algorithms
            respCode = comCommuGetMetrics(queryParam, respBody, dbAccess);
        } else {
            respCode = PSReqWorker.REQUEST_ERR;
            WebServer.win.log.error("-Request command not recognized");
        }

        response.append(respBody.toString());
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Add in DB the new custom community
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuAddCommunity(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the add community function
            success = execAddCommunity(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Add in DB the new custom feature group
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuAddFeatureGroup(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the add Feature Group function
            success = execAddFeatureGroup(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Add in DB the association weight and type between two users
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuAddUserAssociation(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the add User Association function
            success = execAddUserAssociation(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * Calculate the user association weights
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuCalculateUserAssociation(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            //Panagiotis change from:dbAccess.setAutoCommit(false);
            dbAccess.setAutoCommit(false);
            //execute Calculate User Association function
            success = execCalculateUserAssociation(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuCalculateFeatureAssociation(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            //Panagiotis change from:dbAccess.setAutoCommit(false);
            dbAccess.setAutoCommit(false);
            //execute Calculate feature Association function
            success = execCalculateFeatureAssociation(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }
            //disconnect from DB
            dbAccess.disconnect();
        } catch (SQLException e) {  //problem with transaction
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-DB Transaction problem: " + e);
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuMakeCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the make communities function
            success = execMakeCommunities(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuMakeFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the add feature groups function
            success = execMakeFeatureGroups(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuDeleteCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute delete communities function
            success = execDeleteCommunities(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuDeleteFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the delete feature groups function
            success = execDeleteFeatureGroups(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get communities function
            success = execGetCommunities(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get feature groups function
            success = execGetFeatureGroups(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetCommunityProfile(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get community profile function
            success = execGetCommunityProfile(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetFeatureGroupProfile(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get feature group profile function
            success = execGetFeatureGroupProfile(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam
     * @param respBody
     * @param dbAccess
     * @return
     */
    private int comCommuGetCommunityUsers(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get community users function
            success = execGetCommunityUsers(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetFeatureGroupFeatures(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the get feature group features function
            success = execGetFeatureGroupFeatures(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetUserCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the get user's communities function
            success = execGetUserCommunities(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetFeatureFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the get feature's feature groups function
            success = execGetFeatureFeatureGroups(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetAlgorithms(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute get algorithms function
            success = execGetAlgorithms(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private int comCommuGetMetrics(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        int respCode = PSReqWorker.NORMAL;
        try {
            //first connect to DB
            dbAccess.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            return PSReqWorker.SERVER_ERR;
        }

        try {
            boolean success = true;
            dbAccess.setAutoCommit(false);
            //execute the get metrics function
            success = execCommuGetMetrics(queryParam, respBody, dbAccess);
            //if function execute successfully
            if (success) {
                //commit changes
                dbAccess.commit();
            } else {
                //if not success rollback
                dbAccess.rollback();
                respCode = PSReqWorker.REQUEST_ERR;
                WebServer.win.log.warn("-DB rolled back, data not saved");
            }

            //disconnect from DB
            dbAccess.disconnect();
        } catch (Exception e) {
            respCode = PSReqWorker.SERVER_ERR;
            WebServer.win.log.error("-Transaction problem: " + e);
            e.printStackTrace();
        }
        return respCode;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execAddCommunity(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execAddFeatureGroup(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execAddUserAssociation(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam
     * @param respBody
     * @param dbAccess
     * @return
     */
    private boolean execCalculateUserAssociation(VectorMap queryParam, 
            StringBuffer respBody, DBAccess dbAccess) {
        int rowsAffected = 0;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int smetricIdx = queryParam.qpIndexOfKeyNoCase("smetric");
        if (smetricIdx == -1) {
            WebServer.win.log.error("-The parameter smetric is missing: ");
            return false;
        }

        String smetricName = (String) queryParam.getVal(smetricIdx);

        int ftrIdx = queryParam.qpIndexOfKeyNoCase("ftrs");
        String features = null;
        if (ftrIdx != -1) {
            features = (String) queryParam.getVal(ftrIdx);
        }

        boolean success = true;

        VectorMetric metric = PersServer.pbeansLoadader.getVMetrics().get(smetricName);
        if (metric == null) {
            WebServer.win.log.error("-There is no metric with name: " + smetricName);
            return false;
        }

        try {
            generateDistances(dbAccess, clientName, metric, features);
            //pdbAccess.generateBinaryUserRelations( clientName, DBAccess.SIMILARITY_RELATION, DBAccess.BINARY_SIMILARITY_RELATION, threashold );
        } catch (SQLException ex) {
            success = false;
            ex.printStackTrace();
            WebServer.win.log.debug("-Problem executing query: " + ex);
        }

        return success;
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execCalculateFeatureAssociation(VectorMap queryParam, 
            StringBuffer respBody, DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execMakeCommunities(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execMakeFeatureGroups(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execDeleteCommunities(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execDeleteFeatureGroups(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetCommunities(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetFeatureGroups(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetCommunityProfile(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetFeatureGroupProfile(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {

        //TODO: implement by Giannis
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetCommunityUsers(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetUserCommunities(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetFeatureFeatureGroups(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetAlgorithms(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execCommuGetMetrics(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method referring to execution part of process.
     *
     * @param queryParam The parameters of the query.
     * @param respBody The response message that is produced.
     * @param dbAccess The database manager.
     * @return The value of response code.
     */
    private boolean execGetFeatureGroupFeatures(VectorMap queryParam, StringBuffer respBody, 
            DBAccess dbAccess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * 
     * @param dbAccess
     * @param clientName
     * @param metric
     * @param features
     * @throws SQLException 
     */
    private void generateDistances(DBAccess dbAccess, String clientName, VectorMetric metric,
            String features) throws SQLException {
        PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
        pdbAccess.deleteUserAccociations(clientName, DBAccess.RELATION_SIMILARITY);
        pdbAccess.generateUserDistances(clientName, metric, DBAccess.RELATION_SIMILARITY,
                Integer.parseInt(PersServer.pref.getPref("thread_num")), features);
    }

}
