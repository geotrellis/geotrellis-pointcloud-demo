export const isDevelopment = process.env.NODE_ENV === 'development';
export const isProduction = process.env.NODE_ENV === 'production';
export const defaultMapCenter = { lat: 39.9887, lng: -75.153 };
export const metersPerMile = 1609.344;
export const civicInfoApiUrl =
    'https://www.googleapis.com/civicinfo/v2/representatives?address=';
export const civicInfoApiKey = process.env.CIVIC_INFO_API_KEY;
export const censusApiKey = process.env.CENSUS_API_KEY;

export const demoSearchResults =
        {
            neighborhood: 'University City',
            demographics: {
                income: {
                    descriptor: '',
                    quantity: 35000,
                    uom: '',
                },
                medianAge: {
                    descriptor: '',
                    quantity: 45,
                    uom: 'years old',
                },
                population: {
                    descriptor: '',
                    quantity: 100000,
                    uom: 'people',
                },
                racialMakeup: [
                    {
                        descriptor: 'Caucasian',
                        quantity: 1,
                        uom: '%',
                    },
                    {
                        descriptor: 'African American',
                        quantity: 1,
                        uom: '%',
                    },
                    {
                        descriptor: 'Non-White Hispanic',
                        quantity: 1,
                        uom: '%',
                    },
                    {
                        descriptor: 'Asian',
                        quantity: 1,
                        uom: '%',
                    },
                ],
                genderMakeup: [
                    {
                        descriptor: 'M',
                        quantity: 1,
                        uom: '%',
                    },
                    {
                        descriptor: 'F',
                        quantity: 1,
                        uom: '%',
                    },
                ],
            },
            politics: [
                {
                    name: 'Jim Kenney',
                    title: 'Mayor',
                    party: 'Democrat',
                    online: {
                        email: 'someone@some.com',
                        link: 'http://www.google.com',
                        facebook: 'http://www.facebook.com',
                        twitter: 'http://www.twitter.com',
                    },
                    address: '1234 A Street, Philadelphia PA 19147',
                    phone: '123-456-7890',
                },
                {
                    name: 'Bill Greenlee',
                    title: 'Councilperson, Council District 8',
                    party: 'Democrat',
                    online: {
                        email: 'someone@some.com',
                        link: 'http://www.google.com',
                        facebook: 'http://www.facebook.com',
                        twitter: 'http://www.twitter.com',
                    },
                    address: '2345 A Street, Philadelphia PA 19147',
                    phone: '123-456-7890',
                },
                {
                    name: 'Cedar Park Neighbors',
                    title: 'Councilperson, Council District 8',
                    party: 'Democrat',
                    online: {
                        email: 'someone@some.com',
                        link: 'http://www.google.com',
                        facebook: 'http://www.facebook.com',
                        twitter: 'http://www.twitter.com',
                    },
                    address: '2345 A Street, Philadelphia PA 19147',
                    phone: '123-456-7890',
                },
            ],
            pointsOfInterest: {
                police: [
                    {
                        name: '22nd District',
                        address: '5020 Chester Avenue',
                        phone: '(267) 225 5555',
                        location: { lat: 0, lng: 0 },
                    },
                    {
                        name: '19th District',
                        description: '1st Precinct',
                        address: '5020 Chester Avenue',
                        phone: '(267) 225 5555',
                    },
                ],
                fire: [
                    {
                        name: 'Cedar Park Neighbors',
                        description: 'Engine 56',
                        address: '452 S 48th Street',

                    },
                    {
                        name: 'Cedar Park Neighbors',
                        description: 'Engine 57',
                        address: '452 S 49th Street',
                        location: { lat: 0, lng: 0 },
                    },
                ],
                communityOrgs: [
                    {
                        name: 'Cedar Park Neighbors',
                        contactPerson: 'Shawn Markovich',
                        address: '4712 Baltimore Avenue',
                        phone: '(267) 225-5555',
                        location: { lat: 0, lng: 0 },
                    },
                    {
                        name: 'Cedar Park Neighbors',
                        contactPerson: 'Shawn Markovich',
                        address: '4712 Baltimore Avenue',
                        phone: '(267) 225-5555',
                    },
                    {
                        name: 'Cedar Park Neighbors',
                        contactPerson: 'Shawn Markovich',
                        address: '4712 Baltimore Avenue',
                        phone: '(267) 225-5555',
                        location: { lat: 0, lng: 0 },
                    },
                ],
            },
        };
