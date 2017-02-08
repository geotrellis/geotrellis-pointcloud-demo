import _ from 'lodash';
import { demoSearchResults, defaultMapCenter } from 'constants';
import {
    addressDataFromGeocoderResult,
    politicsDataFromCivicInfoResult,
} from 'utils';

import { mapCensusToDemographics } from 'census';
import { SELECT_GEOCODER_RESULT } from '../geocoder/actions';
import {
    COMPLETE_FETCH_CARTO,
    RESET_ADDRESS_DATA,
    SET_RADIUS,
    START_FETCH_CIVIC_INFO,
    COMPLETE_FETCH_CIVIC_INFO,
    FAIL_FETCH_CIVIC_INFO,
    START_CENSUS_FETCH,
    COMPLETE_CENSUS_FETCH,
    UPDATE_DEMOGRAPHICS_DATA,
    SET_HOVER_GEOM,
    CLEAR_HOVER_GEOM,
    SET_HIGHLIGHTED_GEOM,
    CLEAR_HIGHLIGHTED_GEOM,
} from './actions';

const initAddressData = {
    text: '',
    zipCode: '',
    latlng: defaultMapCenter,
};

const initAppPage = {
    searchResults: demoSearchResults,
    address: initAddressData,
    radius: 1.0,
    censusTracts: {
        data: [],
        fetching: false,
        error: {},
    },
    census: [],
    demographics: {
        data: [],
        fetching: false,
        error: {}, // If error is not empty, then show message contained in error
    },
    neighborhoods: {
        data: ['Philadelphia'],
        fetching: false,
        error: {},
    },
    politics: {
        data: [],
        fetching: false,
        error: {},
    },
    communityOrgs: {
        data: [],
        fetching: false,
        error: {},
    },
    policeDistricts: {
        data: [],
        fetching: false,
        error: {},
    },
    fireStations: {
        data: [],
        fetching: false,
        error: {},
    },
    nec: {
        data: [],
        fetching: false,
        error: {},
    },
    nac: {
        data: [],
        fetching: false,
        error: {},
    },
    hoverGeom: null,
    highlightedGeom: null,
    highlightedCard: null,
};

export default function appPage(state = initAppPage, action) {
    switch (action.type) {
        case SELECT_GEOCODER_RESULT:
            return Object.assign({}, state, {
                address: addressDataFromGeocoderResult(action.payload),
            });
        case RESET_ADDRESS_DATA:
            return Object.assign({}, state, {
                address: initAddressData,
            });
        case SET_RADIUS:
            return Object.assign({}, state, {
                radius: action.payload,
            });
        case COMPLETE_FETCH_CARTO:
            return Object.assign({}, state, action.payload);
        case START_FETCH_CIVIC_INFO:
            return Object.assign({}, state, {
                politics: {
                    data: [],
                    fetching: true,
                    error: {},
                },
            });
        case COMPLETE_FETCH_CIVIC_INFO:
            return Object.assign({}, state, {
                politics: {
                    data: politicsDataFromCivicInfoResult(action.payload),
                    fetching: false,
                    error: {},
                },
            });
        case FAIL_FETCH_CIVIC_INFO:
            return Object.assign({}, state, {
                politics: {
                    data: [],
                    fetching: false,
                    error: {},
                },
            });
        case START_CENSUS_FETCH:
            return Object.assign({}, state, action.payload);
        case COMPLETE_CENSUS_FETCH: {
            const val = mapCensusToDemographics(action.payload.census.mappedData);

            return Object.assign({}, state, {
                census: val,
            });
        }
        case UPDATE_DEMOGRAPHICS_DATA: {
            const demographicsObj = {
                income: {
                    descriptor: '',
                    quantity: _.chain(state.census.economic.perCapitaIncome)
                        .reduce((acc, i) => (acc +
                             (i.income * (i.population / state.census.totalPopulation))), 0)
                        .round(0)
                        .value(),
                },
                medianAge: {
                    descriptor: '',
                    quantity: _.chain(state.census.age.median)
                        .reduce((acc, i) => (acc +
                             (i.age * (i.population / state.census.totalPopulation))), 0)
                        .round(0)
                        .value(),
                    uom: 'years old',
                },
                population: {
                    descriptor: '',
                    quantity: state.census.totalPopulation,
                    uom: 'people',
                },
                racialMakeup: [
                    {
                        descriptor: 'White',
                        quantity: state.census.race.white /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Black',
                        quantity: state.census.race.black /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Hispanic',
                        quantity: state.census.ethnicity.latino /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Asian',
                        quantity: state.census.race.asian /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Native',
                        quantity: state.census.race.nativeAmerican /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Hawaiian',
                        quantity: state.census.race.pacificIslander /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: '2+',
                        quantity: state.census.race.multi /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                    {
                        descriptor: 'Other',
                        quantity: state.census.race.other /
                                  state.census.totalPopulation,
                        uom: '%',
                    },
                ],
                genderMakeup: [
                    {
                        descriptor: 'M',
                        quantity: _.round(
                            (state.census.sex.male / state.census.totalPopulation) * 100, 0),
                        uom: '%',
                    },
                    {
                        descriptor: 'F',
                        quantity: _.round(
                            (state.census.sex.female / state.census.totalPopulation) * 100, 0),
                        uom: '%',
                    },
                ],
            };
            return Object.assign({}, state, {
                demographics: {
                    data: [demographicsObj],
                    fetching: false,
                    error: {},
                },
            });
        }
        case SET_HOVER_GEOM:
            return Object.assign({}, state, {
                hoverGeom: action.payload,
            });
        case CLEAR_HOVER_GEOM:
            return Object.assign({}, state, {
                hoverGeom: null,
            });
        case SET_HIGHLIGHTED_GEOM:
            return Object.assign({}, state, {
                highlightedGeom: action.payload.geom,
                highlightedCard: action.payload.card,
            });
        case CLEAR_HIGHLIGHTED_GEOM:
            return Object.assign({}, state, {
                highlightedGeom: null,
                highlightedCard: null,
            });
        default:
            return state;
    }
}
