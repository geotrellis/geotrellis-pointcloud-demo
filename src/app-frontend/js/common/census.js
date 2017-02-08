import _ from 'lodash';
import axios from 'axios';

import { censusApiKey } from 'constants';

// see https://www.census.gov/geo/reference/codes/cou.html
export const censusGeoIds = {
    state: '42',
    county: '101',
};

const censusApiUrl = (censusYear, fields, tracts) => {
    const urlBase = 'https://api.census.gov/data/2014/acs5';
    const urlKey = `key=${censusApiKey}`;
    const urlFields = `get=NAME,${_.join(fields)}`;
    const urlTracts = `for=tract:${_.join(tracts)}`;
    const urlLoc = `in=state:${censusGeoIds.state}+county:${censusGeoIds.county}`;
    const url = `${urlBase}?${urlKey}&${urlFields}&${urlTracts}&${urlLoc}`;
    return url;
};

const censusFieldMap = {
    totalPopulation: 'B01001_001E',
    race: {
        white: 'B02001_002E',
        black: 'B02001_003E',
        asian: 'B02001_005E',
        nativeAmerican: 'B02001_004E',
        pacificIslander: 'B02001_006E',
        other: 'B02001_007E',
        multi: 'B02001_008E',
    },
    ethnicity: {
        latino: 'B03001_003E',
    },
    sex: {
        male: 'B01001_002E',
        female: 'B01001_026E',
    },
    economic: {
        perCapitaIncome: 'B19013_001E',
    },
    age: {
        median: 'B01002_001E',
    },
};

export function transformData(data) {
    const headers = _.head(data);
    const records = _.tail(data);

    // typeMap takes an object in the same shape as the target data object
    // and an ordered list of field names and creates 'getter' functions
    // for each field, which map the an array serving as a record to the field
    const typeMap = (typeShape, dataFlds) => {
        const fldMap = (fld) => {
            const col = _.indexOf(dataFlds, fld);
            return _.curryRight((dataObj, c) => dataObj[c])(col);
        };

        const mapper = (o, mapFn) => _.mapValues(o, v => {
            if (_.isPlainObject(v)) {
                return mapper(v, mapFn);
            }
            return mapFn(v);
        });

        const typeMapInstance = mapper(typeShape, fldMap);

        return (record) => {
            const valueMapper = (o) => _.mapValues(o, v => {
                if (_.isPlainObject(v)) {
                    return valueMapper(v);
                }
                return v(record);
            });

            return valueMapper(typeMapInstance);
        };
    };

    const censusRecordsTypeMap = typeMap(censusFieldMap, headers);
    const mappedRecords = _.map(records, r => censusRecordsTypeMap(r));

    return mappedRecords;
}

export function censusApiQuery(tracts) {
    const url = censusApiUrl(
        '2014',
        _.flatten([
            censusFieldMap.totalPopulation,
            _.values(censusFieldMap.race),
            _.values(censusFieldMap.ethnicity),
            _.values(censusFieldMap.sex),
            _.values(censusFieldMap.economic),
            _.values(censusFieldMap.age),
        ]),
        tracts.data);
    return axios.get(url);
}

export function mapCensusToDemographics(censusData) {
    const demographicsObj = {};
    const simpleSum = (i, j) => _.toNumber(i || 0) + _.toNumber(j || 0);
    const aggregators = {
        totalPopulation: simpleSum,
        race: simpleSum,
        ethnicity: simpleSum,
        sex: simpleSum,
        economic: (dest, src, sourceObj) => _.concat(dest || [], {
            income: _.toNumber(sourceObj.economic.perCapitaIncome),
            population: _.toNumber(sourceObj.totalPopulation),
        }),
        age: (dest, src, sourceObj) => _.concat(dest || [], {
            age: _.toNumber(sourceObj.age.median),
            population: _.toNumber(sourceObj.totalPopulation),
        }),
    };

    const mergeObjects = (targetObj, sourceObj, aggregator) => _.mergeWith(
        targetObj, sourceObj, (obj, src, key, context, source) => {
            const aggFn = aggregator || _.curryRight(aggregators[key])(source);
            if (src instanceof Object) {
                return mergeObjects(obj, src, aggFn);
            }
            return aggFn(obj, src);
        }
    );

    _.each(censusData, p => mergeObjects(demographicsObj, p));

    return demographicsObj;
}
