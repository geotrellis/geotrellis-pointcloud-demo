import React, { Component, PropTypes } from 'react';
/* import { Element, scrollSpy } from 'react-scroll';*/
import { connect } from 'react-redux';
import { browserHistory } from 'react-router';
import { head, isEqual, isEmpty } from 'lodash';

import ScrollButton from 'components/ScrollButton';
import {
    GeoJSONGeometryTypeDef,
    QueryResultTypeDef,
} from 'TypeDefs';
/* import DemographicCards from './Card/Demographics';
 * import PoliticsCards from './Card/Politics';
 * import PointOfInterestCards from './Card/PointsOfInterest';*/
import Map from './Map';
/* import RadiusPicker from './RadiusPicker';*/
import {
    fetchAllData,
    fetchCivicInfo,
    fetchCensusData,
    clearAllGeom,
} from './actions';

/* import PGWLogo from '../../img/geotrellis-logo.png';*/

class App extends Component {
    componentDidMount() {
        const { dispatch, address, radius } = this.props;
        if (!address.text) {
            browserHistory.push('/');
            return;
        }

        dispatch(fetchAllData(address.latlng, radius));
        dispatch(fetchCivicInfo(address.text));
        scrollSpy.update();
    }

    componentWillReceiveProps({ dispatch, address, radius, censusTracts }) {
        if (!isEqual(address.latlng, this.props.address.latlng)) {
            dispatch(clearAllGeom());
            dispatch(fetchAllData(address.latlng, radius));
            dispatch(fetchCivicInfo(address.text));
        } else if (radius !== this.props.radius) {
            dispatch(fetchAllData(address.latlng, radius));
        }

        if (!isEmpty(censusTracts.data) &&
            !isEqual(censusTracts, this.props.censusTracts)) {
            dispatch(fetchCensusData('census', censusTracts));
        }
    }

    render() {
        const {
            dispatch,
            address,
            radius,
            neighborhoods,
            communityOrgs,
            policeDistricts,
            fireStations,
            nec,
            nac,
            politics,
            demographics,
            hoverGeom,
            highlightedGeom,
            highlightedCard,
        } = this.props;

        return (
            <div className="flex-expand-column height-100percent mode-detail">
                <main>
                    <button className="button-analyze">Analyze</button>
                    <div className="sidebar options">
                        <header className="tabs">
                            <div className="tab active">Single Layer</div>
                            <div className="tab">Change Detection</div>
                        </header>
                        <div className="content tab-content content-singlelayer active">
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 1</label>
                                <div>(Tab: Static, Dynamic)</div>
                                <label htmlFor="" className="secondary">Min &amp; Max Elevation</label>
                                <div>(Slider)</div>
                            </div>
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 2</label>
                                <div>(Tab: TIN, DEM)</div>
                            </div>
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 3</label>
                                <div>(Tab: Hillshade, Color Ramp)</div>
                                <label htmlFor="" className="secondary">Opacity</label>
                                <div>(Slider)</div>
                            </div>
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 4</label>
                                <div>(Tab: Hillshade, Color Ramp)</div>
                            </div>
                        </div>
                        <div className="content tab-content content-changedetection">
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 1</label>
                                <div>(Tab: Static, Dynamic)</div>
                                <label htmlFor="" className="secondary">Min &amp; Max Elevation</label>
                                <div>(Slider)</div>
                            </div>
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 2</label>
                                <div>(Tab: TIN, DEM)</div>
                            </div>
                            <div className="option-section">
                                <label htmlFor="" className="primary">Option 3</label>
                                <div>(Tab: Hillshade, Color Ramp)</div>
                                <label htmlFor="" className="secondary">Opacity</label>
                                <div>(Slider)</div>
                            </div>
                        </div>
                    </div>
                    <div className="sidebar analyze active">
                        <header>
                            <div className="sidebar-heading">Analyze</div>
                            <button className="button-cancel">
                                <i className="icon icon-cancel"></i>
                            </button>
                        </header>
                        <div className="draw-buttons">
                            <div className="button-group">
                                <button className="button active">Draw</button>
                                <button className="button">Point</button>
                            </div>
                        </div>
                        <div className="content">
                            <div className="analyze-description active">
                                Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod
                                tincidunt ut laoreet dolore.
                            </div>
                            <div className="analyze-result-wrapper active">
                                <div className="analyze-result">
                                    <div className="analyze-number">7.2</div>
                                    <div className="label primary" htmlFor="">Average Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">6.5</div>
                                    <div className="label primary" htmlFor="">Minimum Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">9.6</div>
                                    <div className="label primary" htmlFor="">Maximum Elevation (ft)</div>
                                </div>
                                <div className="analyze-result">
                                    <div className="analyze-number">10.2</div>
                                    <div className="label primary" htmlFor="">Volume Change (sqft)</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <Map className="map"
                         center={address.latlng}
                         radius={radius}
                         zoom={12}
                         hoverGeom={hoverGeom}
                         highlightedGeom={highlightedGeom}
                    />
                </main>
            </div>

        );
    }
}

App.propTypes = {
    dispatch: PropTypes.func.isRequired,
    address: PropTypes.object.isRequired,
    neighborhoods: QueryResultTypeDef,
    communityOrgs: QueryResultTypeDef,
    policeDistricts: QueryResultTypeDef,
    fireStations: QueryResultTypeDef,
    nec: QueryResultTypeDef,
    nac: QueryResultTypeDef,
    radius: PropTypes.number.isRequired,
    politics: QueryResultTypeDef,
    censusTracts: QueryResultTypeDef,
    demographics: QueryResultTypeDef,
    hoverGeom: GeoJSONGeometryTypeDef,
    highlightedGeom: GeoJSONGeometryTypeDef,
    highlightedCard: PropTypes.string,
};

function mapStateToProps({ appPage }) {
    return appPage;
}

export default connect(mapStateToProps)(App);
