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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.PersServer;
import pserver.WebServer;
import pserver.algorithms.metrics.VectorMetric;
import pserver.data.DBAccess;
import pserver.data.PCommunityDBAccess;
import pserver.data.PFeatureGroupDBAccess;
import pserver.data.PUserDBAccess;
import pserver.data.VectorMap;
import pserver.domain.PUser;
import pserver.logic.PSReqWorker;
import pserver.utilities.ClientCredentialsChecker;
import pserver.utilities.JSon;
import socialpserver.CommunityAPI;
//import pserver.PersServer

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
    private boolean execAddCommunity(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int NameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (NameIdx == -1) {
            WebServer.win.log.error("-The parameter name is missing: ");
            return false;
        }
        String name = (String) queryParam.getVal(NameIdx);

        int UsersIdx = queryParam.qpIndexOfKeyNoCase("users");
        if (UsersIdx == -1) {
            WebServer.win.log.error("-The parameter users is missing: ");
            return false;
        }
        String users = (String) queryParam.getVal(UsersIdx);
        HashSet<String> usersSet = new HashSet<String>(JSon.unjsonize(users, HashSet.class));

        CommunityAPI communityAPI = new CommunityAPI(dbAccess, clientName);

        success = communityAPI.addCustomCommunity(name, usersSet);

//        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<response>" + success + "</response>\n");
        respBody.append("</result>");

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
    private boolean execAddFeatureGroup(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int NameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (NameIdx == -1) {
            WebServer.win.log.error("-The parameter name is missing: ");
            return false;
        }
        String name = (String) queryParam.getVal(NameIdx);

        int FeaturesIdx = queryParam.qpIndexOfKeyNoCase("features");
        if (FeaturesIdx == -1) {
            WebServer.win.log.error("-The parameter features is missing: ");
            return false;
        }
        String Features = (String) queryParam.getVal(FeaturesIdx);
        HashSet<String> featuresSet = new HashSet<String>(JSon.unjsonize(Features, HashSet.class));

        CommunityAPI communityAPI = new CommunityAPI(dbAccess, clientName);

        success = communityAPI.addCustomFeatureGroup(name, featuresSet);
//        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<response>" + success + "</response>\n");
        respBody.append("</result>");

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
    private boolean execAddUserAssociation(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        int rowsAffected = 0;
        boolean success = true;

        //get parameter client
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        //parameters username1,username2 and get user's profile 
        int user1Idx = queryParam.qpIndexOfKeyNoCase("user1");
        if (user1Idx == -1) {
            WebServer.win.log.error("-The parameter user1 is missing: ");
            return false;
        }

        int user2Idx = queryParam.qpIndexOfKeyNoCase("user2");
        if (user2Idx == -1) {
            WebServer.win.log.error("-The parameter user2 is missing: ");
            return false;
        }
        String username1 = (String) queryParam.getVal(user1Idx);
        String username2 = (String) queryParam.getVal(user2Idx);

        //create PUserDBAccess object to get user profile
        PUserDBAccess pudb = new PUserDBAccess(dbAccess);
        PUser user1 = null;
        PUser user2 = null;
        try {
            user1 = pudb.getUserProfile(username1, null, clientName, false);
            user2 = pudb.getUserProfile(username2, null, clientName, false);
        } catch (SQLException ex) {
            WebServer.win.log.debug("-Problem executing query: " + ex);
            return false;
        }

        //Get parameter weight
        int weightIdx = queryParam.qpIndexOfKeyNoCase("weight");
        if (weightIdx == -1) {
            WebServer.win.log.error("-The parameter weight is missing: ");
            return false;
        }
        float weight = Float.parseFloat((String) queryParam.getVal(weightIdx));

        //Get parameter type
        int typeIdx = queryParam.qpIndexOfKeyNoCase("association");
        if (typeIdx == -1) {
            WebServer.win.log.error("-The parameter association is missing: ");
            return false;
        }
        String type = (String) queryParam.getVal(typeIdx);

        //Create a DB statment
        Statement stmt = null;
        try {
            stmt = dbAccess.getConnection().createStatement();

            //Make PCommunity DB Access to call saveUserSimilarity
            PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
            pdbAccess.saveUserSimilarity(user1, user2, weight, clientName, type.hashCode(), stmt);

            stmt.close();
        } catch (SQLException ex) {
            WebServer.win.log.debug("-Problem executing query: " + ex);
            respBody.append("<result>\n");
            respBody.append("<success>false</success>\n");
            respBody.append("</result>");
            return false;
        }
        respBody.append("<result>\n");
        respBody.append("<success>" + success + "</success>\n");
        respBody.append("</result>");

        return success;
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

        int smetricIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (smetricIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }

        String smetricName = (String) queryParam.getVal(smetricIdx);

        //TODO: remove - clean
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
            generateDistances(dbAccess, clientName, metric, features, smetricName.hashCode());
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
        int rowsAffected = 0;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int smetricIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (smetricIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }

        String smetricName = (String) queryParam.getVal(smetricIdx);

        boolean success = true;

        VectorMetric metric = PersServer.pbeansLoadader.getVMetrics().get(smetricName);

        if (metric == null) {
            WebServer.win.log.error("-There is no metric with name: " + smetricName);
            return false;
        }

        try {
            generateFtrDistances(dbAccess, clientName, metric, smetricName.hashCode());
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
    private boolean execMakeCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int algorithmIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (algorithmIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }
        String algorithm = (String) queryParam.getVal(algorithmIdx);

        int typeIdx = queryParam.qpIndexOfKeyNoCase("association");
        if (typeIdx == -1) {
            WebServer.win.log.error("-The parameter association is missing: ");
            return false;
        }
        String type = (String) queryParam.getVal(typeIdx);

        HashMap<String, String> parametersMap = new HashMap<String, String>();
        int parametersIdx = queryParam.qpIndexOfKeyNoCase("parameters");
        String parameters = null;
        if (parametersIdx != -1) {
            parameters = (String) queryParam.getVal(parametersIdx);
            parametersMap.putAll(JSon.unjsonize(parameters, HashMap.class));
        }

        CommunityAPI communityAPI = new CommunityAPI(dbAccess, clientName);
        success = communityAPI.makeCommunities(algorithm, type, parametersMap);

//        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<response>" + success + "</response>\n");
        respBody.append("</result>");

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
    private boolean execMakeFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int algorithmIdx = queryParam.qpIndexOfKeyNoCase("algorithm");
        if (algorithmIdx == -1) {
            WebServer.win.log.error("-The parameter algorithm is missing: ");
            return false;
        }
        String algorithm = (String) queryParam.getVal(algorithmIdx);

        int typeIdx = queryParam.qpIndexOfKeyNoCase("association");
        if (typeIdx == -1) {
            WebServer.win.log.error("-The parameter association is missing: ");
            return false;
        }
        String type = (String) queryParam.getVal(typeIdx);

        HashMap<String, String> parametersMap = new HashMap<String, String>();
        int parametersIdx = queryParam.qpIndexOfKeyNoCase("parameters");
        String parameters = null;
        if (parametersIdx != -1) {
            parameters = (String) queryParam.getVal(parametersIdx);
            parametersMap.putAll(JSon.unjsonize(parameters, HashMap.class));
        }

        CommunityAPI communityAPI = new CommunityAPI(dbAccess, clientName);
        success = communityAPI.makeFeatureGroups(algorithm, type, parametersMap);

//        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<response>" + success + "</response>\n");
        respBody.append("</result>");

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
    private boolean execDeleteCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        boolean success = true;
        int rowsDeleted = 0;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String pattern = null;
        try {
            //if pattern exists delete the specific communitites
            if (patternIdx != -1) {
                pattern = (String) queryParam.getVal(patternIdx);
                pattern = pattern.replaceAll("\\*", "%");
                rowsDeleted = dbAccess.DeleteUserCommunities(clientName, pattern);
            } else {
                //ellse delete all communities
                rowsDeleted = dbAccess.clearUserCommunities(clientName);
            }

        } catch (SQLException e) {
            success = false;
            WebServer.win.log.error("-Problem delete from DB: " + e);
        }

        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>" + rowsDeleted + "</num_of_rows></row>\n");
        respBody.append("</result>");

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
    private boolean execDeleteFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        boolean success = true;
        int rowsDeleted = 0;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String pattern = null;
        try {
            //if pattern exists delete the specific feature groups
            if (patternIdx != -1) {
                pattern = (String) queryParam.getVal(patternIdx);
                pattern = pattern.replaceAll("\\*", "%");
                rowsDeleted = dbAccess.DeleteFeatureCommunities(clientName, pattern);
            } else {
                //ellse delete all feature groups
                rowsDeleted = dbAccess.clearFeatureGroups(clientName);
            }

        } catch (SQLException e) {
            success = false;
            WebServer.win.log.error("-Problem delete from DB: " + e);
        }

        respBody.append(DBAccess.xmlHeader("/resp_xsl/rows.xsl"));
        respBody.append("<result>\n");
        respBody.append("<row><num_of_rows>" + rowsDeleted + "</num_of_rows></row>\n");
        respBody.append("</result>");

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
    private boolean execGetCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String sql = "select " + DBAccess.COMMUNITIES_TABLE_FIELD_COMMUNITY
                + " from " + DBAccess.COMMUNITIES_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.COMMUNITIES_TABLE_FIELD_COMMUNITY
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/communities.xsl"));
        respBody.append("<result>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<community>" + rs.getString(1) + "</community>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        respBody.append("</result>");

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
    private boolean execGetFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String sql = "select " + DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP
                + " from " + DBAccess.FTRGROUPS_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.FTRGROUPS_TABLE_FIELD_FTRGROUP
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/ftrgroups.xsl"));
        respBody.append("<result>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<ftrgroup>" + rs.getString(1) + "</ftrgroup>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        respBody.append("</result>");

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
    private boolean execGetCommunityProfile(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {

        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int nameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (nameIdx == -1) {
            WebServer.win.log.error("-The parameter name is missing: ");
            return false;
        }
        String name = (String) queryParam.getVal(nameIdx);

        String sql = "SELECT feature, feature_value FROM community_profiles  "
                + "WHERE community = '" + name + "' ";

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("ftrpattern");
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + "AND feature LIKE '" + pattern + "' ";
        }

        sql = sql + "AND FK_psclient = '" + clientName + "' "
                + "order by feature_value DESC;";

        Statement stmt = null;
        ResultSet rs = null;

        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/community_profile.xsl"));
        respBody.append("<result>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            respBody.append("<community>" + name + "</community>");
            while (rs.next()) {
                respBody.append("<row>"
                        + "<ftr_name>" + rs.getString("feature") + "</ftr_name>"
                        + "<ftr_value>" + rs.getString("feature_value") + "</ftr_value>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        respBody.append("</result>");

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
    private boolean execGetCommunityUsers(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int nameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (nameIdx == -1) {
            WebServer.win.log.error("-The parameter name is missing: ");
            return false;
        }
        String name = (String) queryParam.getVal(nameIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("usrpattern");
        String sql = "select " + DBAccess.USER_TABLE_FIELD_USER
                + " from " + DBAccess.UCOMMUNITY_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'"
                + " and " + DBAccess.UCOMMUNITY_TABLE_FIELD_COMMUNITY + "='" + name + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.USER_TABLE_FIELD_USER
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/community_users.xsl"));
        respBody.append("<result>\n");
        respBody.append("<community>" + name + "</community>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<user>" + rs.getString(1) + "</user>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
        }
        respBody.append("</result>");

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
    private boolean execGetUserCommunities(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int usernameIdx = queryParam.qpIndexOfKeyNoCase("username");
        if (usernameIdx == -1) {
            WebServer.win.log.error("-The parameter username is missing: ");
            return false;
        }
        String username = (String) queryParam.getVal(usernameIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String sql = "select " + DBAccess.COMMUNITIES_TABLE_FIELD_COMMUNITY
                + " from " + DBAccess.UCOMMUNITY_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'"
                + " and " + DBAccess.USER_TABLE_FIELD_USER + "='" + username + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.COMMUNITIES_TABLE_FIELD_COMMUNITY
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/user_communities.xsl"));
        respBody.append("<result>\n");
        respBody.append("<username>" + username + "</username>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<community>" + rs.getString(1) + "</community>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
        }
        respBody.append("</result>");

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
    private boolean execGetFeatureFeatureGroups(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int featurenameIdx = queryParam.qpIndexOfKeyNoCase("featurename");
        if (featurenameIdx == -1) {
            WebServer.win.log.error("-The parameter featurename is missing: ");
            return false;
        }
        String featurename = (String) queryParam.getVal(featurenameIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("pattern");
        String sql = "select " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP
                + " from " + DBAccess.FTRGROUP_FEATURES_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'"
                + " and " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME + "='" + featurename + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/feature_ftrgroups.xsl"));
        respBody.append("<result>\n");
        respBody.append("<ftr>" + featurename + "</ftr>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<ftrgroup>" + rs.getString(1) + "</ftrgroup>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
        }
        respBody.append("</result>");

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
    private boolean execGetAlgorithms(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        //call aris function
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        CommunityAPI communityAPI = new CommunityAPI(dbAccess, clientName);

        HashMap<String, String> algorithmMap = new HashMap<String, String>();
        algorithmMap.putAll(communityAPI.algorithmDocumentation());

        //make xsl and change header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/algorithms.xsl"));
        respBody.append("<result>\n");

        for (String cAlgorithm : algorithmMap.keySet()) {

            respBody.append("<row>"
                    + "<algorithm_name>" + cAlgorithm + "</algorithm_name>"
                    + "<algorithm_value>" + algorithmMap.get(cAlgorithm) + "</algorithm_value>"
                    + "</row>\n");

        }

        respBody.append("</result>");

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
    private boolean execGetFeatureGroupFeatures(VectorMap queryParam, StringBuffer respBody,
            DBAccess dbAccess) {
        boolean success = true;

        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        String clientName = (String) queryParam.getVal(clntIdx);

        int nameIdx = queryParam.qpIndexOfKeyNoCase("name");
        if (nameIdx == -1) {
            WebServer.win.log.error("-The parameter name is missing: ");
            return false;
        }
        String name = (String) queryParam.getVal(nameIdx);

        String pattern = null;
        int patternIdx = queryParam.qpIndexOfKeyNoCase("ftrpattern");
        String sql = "select " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME
                + " from " + DBAccess.FTRGROUP_FEATURES_TABLE
                + " where " + DBAccess.FIELD_PSCLIENT + "='" + clientName + "'"
                + " and " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_GROUP + "='" + name + "'";
        if (patternIdx != -1) {
            pattern = (String) queryParam.getVal(patternIdx);
            pattern = pattern.replaceAll("\\*", "%");
            sql = sql + " and " + DBAccess.FTRGROUP_FEATURES_TABLE_FIELD_FEATURE_NAME
                    + " like '" + pattern + "'";
        }

        Statement stmt = null;
        ResultSet rs = null;
        //change xsl header
        respBody.append(DBAccess.xmlHeader("/resp_xsl/ftrgroups_features.xsl"));
        respBody.append("<result>\n");
        respBody.append("<ftrgroup>" + name + "</ftrgroup>\n");
        try {
            stmt = dbAccess.getConnection().createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                respBody.append("<row>"
                        + "<ftr>" + rs.getString(1) + "</ftr>"
                        + "</row>\n");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Communities.class.getName()).log(Level.SEVERE, null, ex);
        }
        respBody.append("</result>");

        return success;
    }

    /**
     *
     * Generate user distances between tow user vector profiles
     *
     * @param dbAccess The DB access
     * @param clientName The current client name
     * @param metric The metric to calculate den distance
     * @param features
     * @throws SQLException
     */
    private void generateDistances(DBAccess dbAccess, String clientName, VectorMetric metric,
            String features, int AssociationType) throws SQLException {

        PCommunityDBAccess pdbAccess = new PCommunityDBAccess(dbAccess);
        pdbAccess.deleteUserAccociations(clientName, AssociationType);
        pdbAccess.generateUserDistances(clientName, metric, AssociationType,
                Integer.parseInt(PersServer.pref.getPref("thread_num")), features);
    }

    /**
     * Generate feature distances between tow feature vectors
     *
     * @param dbAccess The DB access
     * @param clientName The current client name
     * @param metric The metric to calculate den distance
     * @throws SQLException
     */
    private void generateFtrDistances(DBAccess dbAccess, String clientName,
            VectorMetric metric, int AssociationType) throws SQLException {

        PFeatureGroupDBAccess pdbAccess = new PFeatureGroupDBAccess(dbAccess);
        pdbAccess.deleteFeatureAccociations(clientName, AssociationType);
        pdbAccess.generateFtrDistances(clientName, metric, AssociationType,
                Integer.parseInt(PersServer.pref.getPref("thread_num")));
    }

}
