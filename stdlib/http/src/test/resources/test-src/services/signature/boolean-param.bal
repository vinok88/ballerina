import ballerina/http;

listener http:MockListener echoEP = new(9090);

@http:ServiceConfig {
    basePath:"/signature"
}
service echo on echoEP {
    resource function echo1 (http:Caller conn, http:Request req, boolean key) {
        http:Response res = new;
    }
}
