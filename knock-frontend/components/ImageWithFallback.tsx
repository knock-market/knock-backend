import React, { useState, useEffect } from 'react';

interface ImageWithFallbackProps extends React.ImgHTMLAttributes<HTMLImageElement> {
    fallbackSrc?: string;
}

const DEFAULT_FALLBACK = 'https://picsum.photos/400/300?grayscale'; // Generic placeholder

const ImageWithFallback: React.FC<ImageWithFallbackProps> = ({
    src,
    fallbackSrc = DEFAULT_FALLBACK,
    alt,
    ...props
}) => {
    const [imgSrc, setImgSrc] = useState(src || fallbackSrc);

    useEffect(() => {
        setImgSrc(src || fallbackSrc);
    }, [src, fallbackSrc]);

    return (
        <img
            {...props}
            src={imgSrc}
            alt={alt}
            onError={() => {
                setImgSrc(fallbackSrc);
            }}
        />
    );
};

export default ImageWithFallback;
