import React, { Component } from 'react';
import './Small.css';



class Small extends Component {
  render() {
    return (
      <article className="Small">
        <header>
          <h1>{this.props.name}</h1>
          <h2>{this.props.price}</h2>
          <p>{this.props.description}</p>
        </header>
      </article>
    );
  }
}

export default Small;
