import React from 'react';
import {Redirect, Switch, Route} from 'react-router-dom';
import Editor from './components/navigation/Editor';
import {InitialRfcCommandsStore} from './contexts/InitialRfcCommandsContext.js';
import {RfcStore, withRfcContext} from './contexts/RfcContext.js';
import RequestPage from './components/RequestPage';
import ConceptsPage from './components/ConceptsPage';
import Dialog from '@material-ui/core/Dialog';
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogContent from '@material-ui/core/DialogContent';
import DialogActions from '@material-ui/core/DialogActions';
import {Button} from '@material-ui/core';
import Loading from './components/navigation/Loading';
import Welcome from './components/onboarding/Welcome';
import UploadOAS from './components/onboarding/upload-oas';
import {ImportedOASContext, ImportedOASStore} from './contexts/ImportedOASContext';
import OverView from './components/onboarding/Overview'

const paths = {
	newRoot: () => '/new',
	example: () => '/examples/:exampleId',
	apiRoot: (base) => base,
	requestPage: (base) => `${base}/requests/:requestId`,
	conceptPage: (base) => `${base}/concepts/:conceptId`,
};

class ExampleLoader extends React.Component {

	state = {
		example: null,
		error: null
	};

	componentDidMount() {
		fetch(`/example-commands/${this.props.match.params.exampleId}-commands.json`)
			.then(response => {
				if (response.ok) {
					return response.text()
						.then(rawString => {
							if (rawString.startsWith('<!DOCTYPE html>')) {
								this.setState({error: true});
							} else {
								this.setState({
									example: rawString
								});
							}
						});
				}
			});
	}

	render() {
		const {example, error} = this.state;

		if (error) {
			return (
				<Dialog open={true}>
					<DialogTitle>Example not found</DialogTitle>
					<DialogContent>The example API you are trying to load could not be found.</DialogContent>
					<DialogActions>
						<Button onClick={() => window.location.reload()}>Reload</Button>
						<Button onClick={() => window.location.href = '/new'} color="secondary">Start New API</Button>
					</DialogActions>
				</Dialog>
			);
		}

		if (example === null) {
			return <Loading/>;
		}
		return (
			<InitialRfcCommandsStore initialCommandsString={example} rfcId="testRfcId">
				<RfcStore>
					<APIEditorRoutes {...this.props} />
				</RfcStore>
			</InitialRfcCommandsStore>
		);
	}
}

class NewApiLoader extends React.Component {
	render() {
		return (
			<ImportedOASContext.Consumer>
				{({providedCommands}) => (
					<InitialRfcCommandsStore initialCommandsString={providedCommands || '[]'} rfcId="testRfcId">
						<RfcStore>
							<APIEditorRoutes {...this.props} />
						</RfcStore>
					</InitialRfcCommandsStore>
				)}
			</ImportedOASContext.Consumer>
		);
	}
}

class APIEditorRoutes extends React.Component {
	render() {

		const {url, path, params} = this.props.match;
		const isNew = path === paths.newRoot();

		const basePath = url;

		//@todo get examples showing
		return (
			<div>
				<Editor basePath={basePath} content={
					<Switch>
						<Route exact path={paths.newRoot(url)} component={() => <>NEW</>}/>
						<Route path={paths.requestPage(url)}
							   component={({match}) =>
								   <RequestPage requestId={match.params.requestId}/>}/>
						<Route path={paths.conceptPage(url)}
							   component={({match}) =>
								   <ConceptsPage conceptId={match.params.conceptId}/>
							   }/>
						<Route component={() => <OverView />}/>
						<Redirect to={paths.apiRoot(url)}/>
					</Switch>
				}/>
			</div>
		);
	}
}

class AppRoutes extends React.Component {
	render() {
		return (
			<div>
				<ImportedOASStore>
					<Switch>
						<Route path={paths.newRoot()} component={NewApiLoader}/>
						<Route path={'/upload-oas'} exact component={UploadOAS}/>
						<Route path={paths.example()} component={ExampleLoader}/>
						<Route path={'/'} exact component={Welcome}/>
						<Redirect to={paths.newRoot()}/>
					</Switch>
				</ImportedOASStore>
			</div>
		);
	}
}

export default AppRoutes;
