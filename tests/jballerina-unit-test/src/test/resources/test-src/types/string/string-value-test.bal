function concatBMP() returns string {
    string prefix = "red ";
    string s = "apple";
    return prefix + s;
}

function nonBMPLength() returns (int) {
    string smiley = "h😀llo";
    return smiley.length();
}

function recordStringValuePut() returns () {
    string smiley = "h😀llo";
    record {| string myField; |} r = {myField: smiley};
    //TODO: return r
}

function testError() returns int {
    string smiley = "h🤷llo";
    error err = error(smiley);
    return err.reason().length();
}

function testArrayStore() returns string[] {
    string[] arr = [];
    arr[0] = "h😀llo";
    return arr;
}

type Person object {
    string helloField = "h🤷llo";
    string lastName;

    function __init(string last) {
        self.lastName = last;
    }

    function appendName(string append) returns string {
        return self.helloField;
    }
};


public function main() {
}

function testObjects() returns string {
 Person p = new("h😀llo");
 return p.appendName("h😀llo");

}
