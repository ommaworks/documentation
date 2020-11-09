const {utils, URLParams, helpers, config, hooks} = service;

const url = 'https://reqres.in/api/users';

const getData = () => {
    return fetch(url)
        .then(res => res.json())
        .catch(err => {return {error: err.toString()}});
};

return new Promise(async resolve => {
    const response = await getData();

    if (response.error) {
        helpers.raiseError({message: 'Could not get data'});
        return resolve();
    }

    const [person1, person2, person3] = response.data.slice(0,3);

    await helpers.useLocals({
        name1: person1.first_name + ' ' + person1.last_name,
        image1: person1.avatar,
        name2: person2.first_name + ' ' + person2.last_name,
        image2: person2.avatar,
        name3: person3.first_name + ' ' + person3.last_name,
        image3: person3.avatar,
    });
    resolve();
});
