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
        {this.props.images.map(image =>
          <img src={process.env.REACT_APP_API + "/home/image/" + image}/>
        )}
      </article>
    );
  }
}

export default Small;
