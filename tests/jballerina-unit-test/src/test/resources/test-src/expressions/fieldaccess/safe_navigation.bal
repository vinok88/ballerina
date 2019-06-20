import ballerina/io;

type Person record {
    int a = 0;
    string fname = "John";
    string lname = "";
    Info|error|() info1 = ();
    Info|() info2 = ();
};

type Info record {
    Address|error|() address1 = ();
    Address|() address2 = ();
};

type Address record {
    string street = "";
    string city = "";
    string country = "Sri Lanka";
};

function testNonErrorPath () returns any|error {
    Address adrs = {city:"Colombo"};
    Info inf = {address1 : adrs};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testNotNilPath () returns any|error {
    Address adrs = {city:"Colombo"};
    Info inf = {address2 : adrs};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() x = p.info2.address2.city;
    return x;
}

function testErrorInMiddle () returns any|error {
    error e = error("custom error");
    Info inf = {address1 : e};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testErrorInFirstVar () returns any|error {
    error e = error("custom error");
    Person|error p = e;
    string|error|() x = p!info1!address1!city;
    return x;
}

function testNilInMiddle () returns [any|error, any|error] {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() x = p.info1!address1!city;
    string|error|() y = p.info2.address2.city;
    return [x, y];
}

function testNilInFirstVar () returns [any|error, any|error] {
    Person|() p = ();
    string|error|() x = p.info1!address1!city;
    string|error|() y = p.info2.address2.city;
    return [x, y];
}

function testSafeNavigatingNilJSON_1 () returns any {
    json j = {};
    return j.foo;
}

function testSafeNavigatingNilJSON_2 () returns any {
    json j = {};
    return j["foo"];
}

function testSafeNavigatingNilJSON_3 () returns any {
    json j = {};
    return j.foo.bar;
}

function testSafeNavigatingNilJSON_4 () returns any {
    json j = {};
    return j["foo"]["bar"];
}

function testSafeNavigatingJSONWithNilInMiddle_1 () returns any {
    json j = {name:"hello"};
    return j.info["name"].fname;
}

function testSafeNavigatingJSONWithNilInMiddle_2 () returns any {
    json j = {name:"hello"};
    return j["info"].name["fname"];
}

function testSafeNavigatingNilMap () returns any {
    map<any> m = {};
    return m["foo"];
}

function testSafeNavigatingWithFuncInovc_1 () returns any|error {
    string|error|() x = getNullablePerson().info1!address1!city;
    return x;
}

function testSafeNavigatingWithFuncInovc_2 () returns string|() {
    json j = {};
    string|() x = j.getKeys()[0];
    return x;
}

function getNullablePerson() returns Person|() {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    return p;
}

type Employee object {
  public function getName() returns string {
     return ("John");
  }
};

function testSafeNavigationOnObject_1() returns string {
  Employee? p;
  p = new Employee();
    return p.getName() ?: "null name";
}

function testSafeNavigationOnObject_2() returns string {
  Employee? p = ();
  return p.getName() ?: "null name";
}

function testSafeNavigationOnObject_3() returns string {
  Employee?|error p = ();
  p = new Employee();
  string|error? name = p!getName();
  return name is string ? name : "null name";
}

function testSafeNavigationOnObject_4() returns string {
  error e = error("");
  Employee|error p = e;
  string|error name = p!getName();
  return name is string ? name : "error name";
}

function testSafeNavigateArray_1() returns Person? {
    Person[]|() p = ();
    return p[0];
}

function testSafeNavigateArray_2() returns string? {
    Person[]|() p = ();
    return p[0].info2.address2.city;
}

function testSafeNavigateOnErrorOrNull() returns string? {
    error|() e = ();
    return e.reason();
}

function testSafeNavigateOnJSONArrayOfArray() returns json {
    json j = {"values" : [ ["Alex", "Bob"] ] };
    return j.values[0][1];
}

function testJSONNilLiftingOnLHS_1() returns [json, json, json, json] {
    json j1 = {};
    j1.info["address1"].city = "Colombo";

    json j2 = { info:{} };
    j2.info["address2"].city = "Kandy";

    json j3 = { info : { address3: {} } };
    j3.info["address3"].city = "Galle";

    json j4 = {};
    j4.info["address4"].city = "Jaffna";

    return [j1, j2, j3, j4];
}

function testJSONNilLiftingOnLHS_2() returns json {
    json j = [];
    j[2].address[3].city = "Colombo";
    return j;
}

function testNonExistingMapKeyWithIndexAccess() returns string? {
    map<string> m = {};
    return m["a"];
}

function testNonExistingMapKeyWithFieldAccess() returns string {
    map<string> m = {};
    return m.a;
}

function testMapNilLiftingOnLHS_1() returns map<any> {
    map<any> m = {};
    m.name = "John";
    return m;
}

function testMapNilLiftingOnLHS_2() returns map<any> {
    map<json> m = {};
    m["name"].fname = "John";
    return m;
}

function testMapNilLiftingOnLHS_3() returns map<map<any>?> {
    map<map<any>?> m = {};
    m["name"].fname = "John";
    return m;
}

function testMapNilLiftingOnLHS_4() returns map<A?> {
    map<A?> m = {};
    m["name"].fname = "John";
    return m;
}

function testMapNilLiftingOnLHS_5() returns map<string>? {
    map<string>? m = ();
    m["name"] = "John";
    return m;
}

type A record {
    map<any>? foo = ();
};

function testMapInRecordNilLiftingOnLHS_1() returns A {
    A a = {};
    a.foo = {"name" : "John"};
    a.foo.name = "Doe";
    return a;
}

function testMapInRecordNilLiftingOnLHS_2() returns A? {
    A? a = ();
    a.foo = {"name" : "John"};
    a.foo.name = "Doe";
    return a;
}

function testFunctionInvocOnJsonNonExistingField (json inputJson) returns [json, string, string[]] {
    json j = {name:"John"};
    string s = j.foo.bar.toString();
    string[] keys = j.foo.bar.getKeys();
    return [j, s, keys];
}

type Student object {
    public string name = "";
    public int marks = 60;

    function increaseMarks() {
        self.marks = self.marks + 1;
    }
};

function testFunctionInvocOnNullabeType() returns int {
    Student s1 = new;
    Student|() s2 = s1;
    s2.increaseMarks();
    return s2.marks ?: -1;
}

function testUpdatingNullableRecordField_1() returns any {
    Address adrs = {street:"Palm Grove"};
    Info inf = {address2 : adrs};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    p.info2.address2.city = "Kandy";
    return p;
}

function testUpdatingNullableRecordField_2() returns any {
    Person prsn = {};
    Person|() p = prsn;
    p.info2.address2.city = "Kandy";
    return p;
}

type PersonObject object {
    public int a = 0;
    public string fname = "John";
    public string lname = "";
    public InfoObject|()|error info1 = ();
    public InfoObject|() info2;

    function __init(InfoObject|() info2) {
        self.info2 = info2;
    }
};

type InfoObject object {
    public AddressObject|()|error address1 = ();
    public AddressObject|() address2;

    function __init(AddressObject address2) {
        self.address2 = address2;
    }
};

type AddressObject object {
    public string street;
    public string city = "";
    public string country = "Sri Lanka";

    function __init(string street) {
        self.street = street;
    }
};

function testUpdatingNullableObjectField_1() returns any {
    AddressObject adrs = new("Palm Grove");
    InfoObject inf = new(adrs);
    PersonObject prsn = new(inf);
    PersonObject|() p = prsn;
    p.info2.address2.city = "Kandy";
    return p;
}

function testUpdatingNullableObjectField_2() returns any {
    PersonObject|() p = new PersonObject(());
    p.info2.address2.city = "Kandy";
    return p;
}

function getJsonValue() returns json|error {
    return 10;
}

function testSafeNavigationOnFieldAccess() returns json|error {
    return getJsonValue()!foo;
}

function testSafeNavigationOnIndexBasedAccess() returns json|error {
    (json|error)?[] data = [getJsonValue()];
    return data[0]!foo;
}
