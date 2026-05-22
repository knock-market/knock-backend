import React, { useState, useEffect } from 'react';
import { DEFAULT_IMAGE } from '../constants';

interface ImageWithFallbackProps extends React.ImgHTMLAttributes<HTMLImageElement> {
    fallbackSrc?: string;
}

const ImageWithFallback: React.FC<ImageWithFallbackProps> = ({
    src,
    fallbackSrc = DEFAULT_IMAGE,
    alt,
    loading = 'lazy',
    decoding = 'async',
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
            alt={alt || ''}
            loading={loading}
            decoding={decoding}
            onError={() => {
                setImgSrc(fallbackSrc);
            }}
        />
    );
};

export default ImageWithFallback;
