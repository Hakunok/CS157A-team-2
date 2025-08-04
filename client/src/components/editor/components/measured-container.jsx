import * as React from "react";
import { useContainerSize } from "../hooks/use-container-size";

const MeasuredContainer = ({ as: Component, name, children, style = {}, ...props }) => {
  const innerRef = React.useRef(null);
  const rect = useContainerSize(innerRef.current);

  const customStyle = {
    [`--${name}-width`]: `${rect.width}px`,
    [`--${name}-height`]: `${rect.height}px`,
  };

  return (
      <Component {...props} ref={innerRef} style={{ ...customStyle, ...style }}>
        {children}
      </Component>
  );
};

MeasuredContainer.displayName = "MeasuredContainer";

export { MeasuredContainer };