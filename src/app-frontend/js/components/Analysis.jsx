import React, { Component, PropTypes } from 'react';

import { Slider, Button, Tabs, TabList, TabPanel, Tab } from "@blueprintjs/core";

import {
    setAnalysisOn,
} from './actions';

export default class Analysis extends Component {
    constructor() {
        super();

        this.onCancelClicked = this.onCancelClicked.bind(this);
    }

    onCancelClicked() {
        const { dispatch } = this.props;
        console.log("CANCEL ANALYSIS");
        dispatch(setAnalysisOn(false));
    }

    render() {
        const {
            analysisOn,
            polygon,
            point,
            results } = this.props;

        if(!analysisOn) { return null; }

        let body = null;
        if(polygon || point) {
            body =
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
        } else {
            body =
                <div className="analyze-description active">
                        <p>Draw an area or drop a point on the map to calculate
                            statistics for that location.</p>
                </div>
        }

        return (
            <div className="sidebar analyze active">
                <header>
                    <div className="sidebar-heading">Analyze</div>
                    <button className="button-cancel" onClick={this.onCancelClicked}>
                        <i className="icon icon-cancel"></i>
                    </button>
                </header>

                <div className="content">
                    {body}
                </div>
            </div>
        );
    }
}

Analysis.propTypes = {
    dispatch: PropTypes.func.isRequired,
    analysisOn: PropTypes.bool.isRequired,
    results: PropTypes.object.isRequired,
}
