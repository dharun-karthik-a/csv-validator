const rewire = require("rewire")

const convert = rewire("../src/main/public/js/Convert.js");

describe("convert", () => {
    it("should convert csv to json", () => {
        let lines = `a,b,c\n1,xyz,1.2\n2,pqr,2.4`
        let csvToJson = convert.__get__("csvToJson")
        let expected = [{a: '1', b: 'xyz', c: '1.2'}, {a: '2', b: 'pqr', c: '2.4'}]

        let actual = csvToJson(lines);

        expect(actual).toEqual(expected)
    })

    it("should convert csv having commas inside double quotes to json", () => {
        let lines = `a,b,c\n1,"xyz,lmn",1.2\n2,"pqr,jhk","2.4,a"`
        let csvToJson = convert.__get__("csvToJson")
        let expected = [{a: '1', b: 'xyz,lmn', c: '1.2'}, {a: '2', b: 'pqr,jhk', c: '2.4,a'}]

        let actual = csvToJson(lines);

        expect(actual).toEqual(expected)
    })
})

describe("convert payload to jsonarray", () => {
    it("should convert payload to jsonArray", () => {
        let payload = {
            "Product id": { "field": "Product Id", "type": "AlphaNumeric" },
            "CName": { "field": "Product Description", "type": "Alphanumeric" }
        }
        let convertPayloadToJsonArray = convert.__get__("convertPayloadToJsonArray")
        let expected = [{ "field": "Product Id", "type": "AlphaNumeric" },
        { "field": "Product Description", "type": "Alphanumeric" }]

        let actual = convertPayloadToJsonArray(payload);

        expect(actual).toEqual(expected)
    })

    it("should convert payload with dependencies to jsonArray", () => {
        let payload = {
            "Product id": { "field": "Product Id", "type": "AlphaNumeric" },
            "CName": {
                "field": "Product Description", "type": "Alphanumeric",
                "dependentOn": "Product Id", "expectedDependentFieldValue": "Y", "expectedCurrentFieldValue": "null"
            }
        }
        let convertPayloadToJsonArray = convert.__get__("convertPayloadToJsonArray")
        let expected = [{ "field": "Product Id", "type": "AlphaNumeric" },
        {
            "field": "Product Description", "type": "Alphanumeric",
            "dependencies": [{ "dependentOn": "Product Id", "expectedDependentFieldValue": "Y", "expectedCurrentFieldValue": "null" }],
        }]

        let actual = convertPayloadToJsonArray(payload);

        expect(actual).toEqual(expected)
    })
})

describe("convert to lowercase", () => {
    it("should convert data to lowercase", () => {
        let data = "SampleText"
        let lowerCase = convert.__get__("lowerCase")
        let expected = "sampletext"

        let actual = lowerCase(data);

        expect(actual).toEqual(expected)
    })

    it("should not convert data to lowercase if field is pattern", () => {
        let data = "SampleText"
        let field = "pattern"
        let lowerCase = convert.__get__("lowerCase")
        let expected = "SampleText"

        let actual = lowerCase(data, field);

        expect(actual).toEqual(expected)
    })

    it("should convert data to lowercase if field is anything except pattern", () => {
        let data = "SampleText"
        let field = "fieldName"
        let lowerCase = convert.__get__("lowerCase")
        let expected = "sampletext"

        let actual = lowerCase(data, field);

        expect(actual).toEqual(expected)
    })
})